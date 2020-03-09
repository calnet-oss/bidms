/*
 * Copyright (c) 2020, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.bidms.app.sgs.executor;

import edu.berkeley.bidms.app.common.service.ValidationService;
import edu.berkeley.bidms.app.sgs.config.properties.SgsConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.SorConfigProperties;
import edu.berkeley.bidms.app.sgs.service.RegistrySqlTextService;
import edu.berkeley.bidms.common.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Perform SOR queries.
 */
@Service
public class QueryExecutorService extends ExecutorService implements ApplicationContextAware {
    private final Logger log = LoggerFactory.getLogger(QueryExecutorService.class);

    private ApplicationContext applicationContext;
    private JdbcTemplate registryJdbcTemplate;
    private RegistrySqlTextService registrySqlTextService;
    private ValidationService validationService;

    public QueryExecutorService(SgsConfigProperties sgsConfig, JdbcTemplate registryJdbcTemplate, RegistrySqlTextService registrySqlTextService, ValidationService validationService) {
        super(sgsConfig);
        this.registryJdbcTemplate = registryJdbcTemplate;
        this.registrySqlTextService = registrySqlTextService;
        this.validationService = validationService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Get hash differences which means differences between the {@code
     * SORObjectChecksum} hash values and the hash values in the {@code
     * SORObject} table. The SORObjectChecksum table is populated by "step 1"
     * hash queries.
     *
     * @param sorId sorId of the SOR.
     * @return A {@link HashDifferences} instance containing a collectio of
     * {@link HashDifference} values.
     */
    public HashDifferences getHashDifferences(int sorId) {
        final int chkSorObjKeyIdx = 1;
        final int soSorObjKeyIdx = 2;
        final int newHashIdx = 3;
        final int soIsDeletedIdx = 4;
        final int newHashTimeMarkerIdx = 5;
        HashDifferences hashDifferences = new HashDifferences();
        SqlRowSet rowSet = registryJdbcTemplate.queryForRowSet(registrySqlTextService.hashDifferencesForSor("SELECT chkSorObjKey, soSorObjKey, newHash, soIsDeleted, newHashTimeMarker FROM SorHashDifferenceView WHERE sorId = ?"), sorId);
        while (rowSet.next()) {
            final String soSorObjKey = rowSet.getString(soSorObjKeyIdx);
            final String chkSorObjKey = rowSet.getString(chkSorObjKeyIdx);
            final boolean soIsDeleted = rowSet.getBoolean(soIsDeletedIdx);
            if (chkSorObjKey != null && soSorObjKey == null) {
                // not in SORObject table but should be
                hashDifferences.addHashDifference(chkSorObjKey, rowSet.getLong(newHashIdx), true, rowSet.getTimestamp(newHashTimeMarkerIdx));
            } else if (chkSorObjKey == null && soSorObjKey != null) {
                // if already soft-deleted, it's a no-op
                if (!soIsDeleted) {
                    // in SORObject table but shouldn't be because it's no longer in the SOR
                    hashDifferences.getDeletedSorObjectKeys().add(soSorObjKey);
                }
            } else {
                // in SORObject table but the hash code has changed
                hashDifferences.addHashDifference(chkSorObjKey, rowSet.getLong(newHashIdx), false, rowSet.getTimestamp(newHashTimeMarkerIdx));
            }
        }
        return hashDifferences;
    }

    /**
     * Count the number of rows in the SORObject table for a particular SOR.
     * ("Soft-deleted" rows are excluded from the count.)
     *
     * @param sorId sorId of the SOR
     * @return The number of SORObject rows (excluding "soft-deleted" rows).
     */
    public Integer getSorObjectCount(int sorId) {
        return registryJdbcTemplate.queryForObject(registrySqlTextService.sorObjectCountForSorSql("SELECT count(*) FROM SORObject WHERE isDeleted=false AND sorId=?"), Integer.class, sorId);
    }

    /**
     * Create a {@code SORObject} row.
     *
     * @param sorConfig  SOR configuration.
     * @param sorId      sorId of the SOR.
     * @param sorObjKey  Primary key of the SOR object.
     * @param hash       The current hash value from the {@code
     *                   SORObjectChecksum} table.
     * @param rowContent {@link QueryEntryContent} containing the SOR object
     *                   data.
     * @throws QueryExecutorException If an error occurred.
     */
    public void createSorObject(SorConfigProperties sorConfig, int sorId, String sorObjKey, Long hash, QueryEntryContent<?> rowContent) throws QueryExecutorException {
        try {
            validateRowContent(sorConfig.getSorName(), sorObjKey, rowContent);
        } catch (ValidationException e) {
            throw new QueryExecutorException(e);
        }
        registryJdbcTemplate.update(registrySqlTextService.insertSorObjectSql("INSERT INTO SORObject(sorId, sorObjKey, sorQueryTime, hash, hashVersion, jsonVersion, objJson) VALUES(?,?,?,?,?,?,CAST(? AS jsonb))"), sorId, sorObjKey, rowContent.getQueryTime(), hash, 1, 1, rowContent.getObjJson());
    }

    /**
     * Update a {@code SORObject} row.
     *
     * @param sorConfig  SOR configuration.
     * @param sorId      sorId of the SOR.
     * @param sorObjKey  Primary key of the SOR object.
     * @param hash       The current hash value from the {@code
     *                   SORObjectChecksum} table.
     * @param rowContent {@link QueryEntryContent} containing the SOR object
     *                   data.
     * @throws QueryExecutorException If an error occurred.
     */
    public void updateSorObject(SorConfigProperties sorConfig, int sorId, String sorObjKey, Long hash, QueryEntryContent<?> rowContent) throws QueryExecutorException {
        try {
            validateRowContent(sorConfig.getSorName(), sorObjKey, rowContent);
        } catch (ValidationException e) {
            throw new QueryExecutorException(e);
        }
        if (registryJdbcTemplate.update(registrySqlTextService.updateSorObjectSql("UPDATE SORObject SET sorQueryTime=?, hash=?, hashVersion=?, jsonVersion=?, isDeleted=false, objJson=CAST(? AS jsonb) WHERE sorId=? AND sorObjKey=?"), rowContent.getQueryTime(), hash, 1, 1, rowContent.getObjJson(), sorId, sorObjKey) <= 0) {
            throw new QueryExecutorException("Couldn't update SORObject sorId=" + sorId + ", sorObjKey" + sorObjKey + ".  Did it get deleted in the middle of our operations?");
        }
    }

    /**
     * Soft-delete a {@code SORObject} row.  Soft-delete means setting
     * isDeleted flag to true.
     *
     * @param sorConfig SOR configuration.
     * @param sorId     sorId of the SOR.
     * @param sorObjKey Primary key of the SOR object.
     * @return true of the row was found in {@code SORObject} table and
     * marked as soft deleted.  If false, row does not exist and has remained
     * "hard deleted."
     * @throws QueryExecutorException If an error occurred.
     */
    public boolean deleteSorObject(SorConfigProperties sorConfig, int sorId, String sorObjKey) throws QueryExecutorException {
        if (registryJdbcTemplate.update(registrySqlTextService.softDeleteSorObjectSql("UPDATE SORObject SET isDeleted=true WHERE sorId=? AND sorObjKey=?"), sorId, sorObjKey) <= 0) {
            log.warn("Couldn't soft-delete SORObject sorId=" + sorId + ", sorObjKey=" + sorObjKey + " because it wasn't found.");
            return false;
        }
        return true;
    }

    /**
     * Delete a {@code SORObjectChecksum} row.
     *
     * @param sorId     sorId of the SOR.
     * @param sorObjKey Primary key of the SOR object.
     * @return true if the row was found and deleted.
     */
    public boolean deleteSorObjectChecksum(int sorId, String sorObjKey) {
        return registryJdbcTemplate.update(registrySqlTextService.deleteSorObjectChecksumForKeySql("DELETE FROM SorObjectChecksum WHERE sorId=? AND sorObjKey=?"), sorId, sorObjKey) > 0;
    }

    private <N> void validateRowContent(String sorName, String sorObjKey, QueryEntryContent<N> rowContent) throws ValidationException {
        rowContent.validate(validationService);
        if (!sorName.equals(rowContent.getSorName())) {
            throw new ValidationException("sorName incorrect in query row content: expected " + sorName + ", got " + sorName);
        }
        if (!sorObjKey.equals(rowContent.getSorObjKey())) {
            throw new ValidationException("sorObjKey incorrect in query row content: expected " + sorObjKey + ", got " + sorObjKey);
        }
        if (rowContent.getNativeContent() == null) {
            throw new ValidationException("native row content is null");
        }
    }

    /**
     * Contains row counts and existing hash values in the {@code SORObject}
     * table.
     */
    public static class ExistingSorObjects {
        // isDeleted=true keys in SORObject
        private SortedSet<String> existingSoftDeletedSorObjectKeys;
        // isDeleted=false keys in SORObject
        private SortedSet<String> existingSorObjectKeys;
        // hash values from SORObjectChecksum whether in SORObject table or not
        private Map<String, Long> existingSorObjectHashes;

        public ExistingSorObjects(SortedSet<String> existingSoftDeletedSorObjectKeys, SortedSet<String> existingSorObjectKeys, Map<String, Long> existingSorObjectHashes) {
            this.existingSoftDeletedSorObjectKeys = existingSoftDeletedSorObjectKeys;
            this.existingSorObjectKeys = existingSorObjectKeys;
            this.existingSorObjectHashes = existingSorObjectHashes;
        }

        public SortedSet<String> getExistingSoftDeletedSorObjectKeys() {
            return existingSoftDeletedSorObjectKeys;
        }

        public SortedSet<String> getExistingSorObjectKeys() {
            return existingSorObjectKeys;
        }

        public Map<String, Long> getExistingSorObjectHashes() {
            return existingSorObjectHashes;
        }
    }

    /**
     * Gathers row counts and existing hash values from the {@code SORObject}
     * table.
     *
     * @param sorId sorId of the SOR.
     * @return {@link ExistingSorObjects} which contains row counts and
     * existing hash values in the {@code SORObject} table.
     */
    public ExistingSorObjects queryForExistingSorObjectKeysAndHashes(int sorId) {
        /* Query for existing local sor object keys because in full mode, we need to know to create, update, or delete keys locally.
         * We split the local keys into two categories: normal and soft-deleted.
         * Soft-deleted keys are a special case where they will be undeleted as an update if they newly exist in the SOR again.
         * If they don't exist in the SOR, then it's a no-op (they stay soft-deleted locally). */
        // isDeleted=true keys in SORObject
        SortedSet<String> existingSoftDeletedSorObjectKeys = new TreeSet<>(registryJdbcTemplate.queryForList(registrySqlTextService.softDeletedSorObjectKeysForSorSql("SELECT sorObjKey FROM SORObject WHERE isDeleted=true AND sorId=?"), String.class, sorId));
        // isDeleted=false keys in SORObject
        SortedSet<String> existingSorObjectKeys = new TreeSet<>(registryJdbcTemplate.queryForList(registrySqlTextService.nondeletedSorObjectKeysForSorSql("SELECT sorObjKey FROM SORObject WHERE isDeleted=false AND sorId=?"), String.class, sorId));
        // hash values from SORObjectChecksum whether in SORObject table or not
        Map<String, Long> existingSorObjectHashes = new TreeMap<>();
        // Query for existing hash values from the SORObjectChecksum table.
        // We do want a SORObjectChecksum row even if it's soft or hard deleted in SORObject.
        registryJdbcTemplate.query(registrySqlTextService.hashValuesForSorSql("SELECT sorObjKey, hash FROM SorObjectChecksum WHERE sorId = ?"), new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String sorObjKey = rs.getString(1);
                Long hash = (Long) rs.getObject(2);
                if (hash != null && hash != 0) {
                    existingSorObjectHashes.put(sorObjKey, hash);
                }
            }
        }, sorId);
        return new ExistingSorObjects(existingSoftDeletedSorObjectKeys, existingSorObjectKeys, existingSorObjectHashes);
    }
}
