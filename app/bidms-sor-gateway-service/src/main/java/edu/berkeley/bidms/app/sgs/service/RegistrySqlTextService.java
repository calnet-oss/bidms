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
package edu.berkeley.bidms.app.sgs.service;

import org.springframework.stereotype.Service;

/**
 * Provides SGS SQL queries to the Registry database.  The purpose of this is
 * to be able to override default queries and secondarily provide a central
 * reference point to find all the SGS SQL queries that are in use.
 */
@Service
public class RegistrySqlTextService {
    private String sorIdBySorNameSql;

    public String sorIdBySorNameSql(String defaultSql) {
        return sorIdBySorNameSql != null ? sorIdBySorNameSql : defaultSql;
    }

    public void setSorIdBySorNameSql(String sorIdBySorNameSql) {
        this.sorIdBySorNameSql = sorIdBySorNameSql;
    }

    private String hashDifferencesForSorSql;

    public String hashDifferencesForSor(String defaultSql) {
        return hashDifferencesForSorSql != null ? hashDifferencesForSorSql : defaultSql;
    }

    public void setHashDifferencesForSorSql(String hashDifferencesForSorSql) {
        this.hashDifferencesForSorSql = hashDifferencesForSorSql;
    }

    private String lastTimeMarkerForSorSql;

    public String lastTimeMarkerForSorSql(String defaultSql) {
        return lastTimeMarkerForSorSql != null ? lastTimeMarkerForSorSql : defaultSql;
    }

    public void setLastTimeMarkerForSorSql(String lastTimeMarkerForSorSql) {
        this.lastTimeMarkerForSorSql = lastTimeMarkerForSorSql;
    }

    private String deleteSorObjectChecksumQueryForSorSql;

    public String deleteSorObjectChecksumForSorSql(String defaultSql) {
        return deleteSorObjectChecksumQueryForSorSql != null ? deleteSorObjectChecksumQueryForSorSql : defaultSql;
    }

    public void setDeleteSorObjectChecksumQueryForSorSql(String deleteSorObjectChecksumQueryForSorSql) {
        this.deleteSorObjectChecksumQueryForSorSql = deleteSorObjectChecksumQueryForSorSql;
    }

    private String deleteAllSorObjectChecksumsForSorSql;

    public String deleteAllSorObjectChecksumsForSorSql(String defaultSql) {
        return deleteAllSorObjectChecksumsForSorSql != null ? deleteAllSorObjectChecksumsForSorSql : defaultSql;
    }

    public void setDeleteAllSorObjectChecksumsForSorSql(String deleteAllSorObjectChecksumsForSorSql) {
        this.deleteAllSorObjectChecksumsForSorSql = deleteAllSorObjectChecksumsForSorSql;
    }

    private String copySorObjectChecksumsForSorSql;

    // for PostgreSQL only
    public String copySorObjectChecksumsForSorSql(String defaultSql) {
        return copySorObjectChecksumsForSorSql != null ? copySorObjectChecksumsForSorSql : defaultSql;
    }

    public void setCopySorObjectChecksumsForSorSql(String copySorObjectChecksumsForSorSql) {
        this.copySorObjectChecksumsForSorSql = copySorObjectChecksumsForSorSql;
    }

    private String insertSorObjectChecksumForSorSql;

    // for non-PostgreSQL
    public String insertSorObjectChecksumForSorSql(String defaultSql) {
        return insertSorObjectChecksumForSorSql != null ? insertSorObjectChecksumForSorSql : defaultSql;
    }

    public void setInsertSorObjectChecksumForSorSql(String insertSorObjectChecksumForSorSql) {
        this.insertSorObjectChecksumForSorSql = insertSorObjectChecksumForSorSql;
    }

    private String checkSorObjectChecksumExistenceSql;

    public String checkSorObjectChecksumExistenceSql(String defaultSql) {
        return checkSorObjectChecksumExistenceSql != null ? checkSorObjectChecksumExistenceSql : defaultSql;
    }

    public void setCheckSorObjectChecksumExistenceSql(String checkSorObjectChecksumExistenceSql) {
        this.checkSorObjectChecksumExistenceSql = checkSorObjectChecksumExistenceSql;
    }

    private String insertSorObjectChecksumSql;

    public String insertSorObjectChecksumSql(String defaultSql) {
        return insertSorObjectChecksumSql != null ? insertSorObjectChecksumSql : defaultSql;
    }

    public void setInsertSorObjectChecksumSql(String insertSorObjectChecksumSql) {
        this.insertSorObjectChecksumSql = insertSorObjectChecksumSql;
    }

    private String updateSorObjectChecksumSql;

    public String updateSorObjectChecksumSql(String defaultSql) {
        return updateSorObjectChecksumSql != null ? updateSorObjectChecksumSql : defaultSql;
    }

    public void setUpdateSorObjectChecksumSql(String updateSorObjectChecksumSql) {
        this.updateSorObjectChecksumSql = updateSorObjectChecksumSql;
    }

    private String sorObjectCountForSorSql;

    public String sorObjectCountForSorSql(String defaultSql) {
        return sorObjectCountForSorSql != null ? sorObjectCountForSorSql : defaultSql;
    }

    public void setSorObjectCountForSorSql(String sorObjectCountForSorSql) {
        this.sorObjectCountForSorSql = sorObjectCountForSorSql;
    }

    private String insertSorObjectSql;

    public String insertSorObjectSql(String defaultSql) {
        return insertSorObjectSql != null ? insertSorObjectSql : defaultSql;
    }

    public void setInsertSorObjectSql(String insertSorObjectSql) {
        this.insertSorObjectSql = insertSorObjectSql;
    }

    public String updateSorObjectSql;

    public String updateSorObjectSql(String defaultSql) {
        return updateSorObjectSql != null ? updateSorObjectSql : defaultSql;
    }

    public void setUpdateSorObjectSql(String updateSorObjectSql) {
        this.updateSorObjectSql = updateSorObjectSql;
    }

    public String softDeleteSorObjectSql;

    public String softDeleteSorObjectSql(String defaultSql) {
        return softDeleteSorObjectSql != null ? softDeleteSorObjectSql : defaultSql;
    }

    public void setSoftDeleteSorObjectSql(String softDeleteSorObjectSql) {
        this.softDeleteSorObjectSql = softDeleteSorObjectSql;
    }

    public String softDeletedSorObjectKeysForSorSql;

    public String softDeletedSorObjectKeysForSorSql(String defaultSql) {
        return softDeletedSorObjectKeysForSorSql != null ? softDeletedSorObjectKeysForSorSql : defaultSql;
    }

    public void setSoftDeletedSorObjectKeysForSorSql(String softDeletedSorObjectKeysForSorSql) {
        this.softDeletedSorObjectKeysForSorSql = softDeletedSorObjectKeysForSorSql;
    }

    public String nondeletedSorObjectKeysForSorSql;

    public String nondeletedSorObjectKeysForSorSql(String defaultSql) {
        return nondeletedSorObjectKeysForSorSql != null ? nondeletedSorObjectKeysForSorSql : defaultSql;
    }

    public void setNondeletedSorObjectKeysForSorSql(String nondeletedSorObjectKeysForSorSql) {
        this.nondeletedSorObjectKeysForSorSql = nondeletedSorObjectKeysForSorSql;
    }

    public String hashValuesForSorSql;

    public String hashValuesForSorSql(String defaultSql) {
        return hashValuesForSorSql != null ? hashValuesForSorSql : defaultSql;
    }

    public void setHashValuesForSorSql(String hashValuesForSorSql) {
        this.hashValuesForSorSql = hashValuesForSorSql;
    }

    public String deleteSorObjectChecksumForKeySql;

    public String deleteSorObjectChecksumForKeySql(String defaultSql) {
        return deleteSorObjectChecksumForKeySql != null ? deleteSorObjectChecksumForKeySql : defaultSql;
    }

    public void setDeleteSorObjectChecksumForKeySql(String deleteSorObjectChecksumForKeySql) {
        this.deleteSorObjectChecksumForKeySql = deleteSorObjectChecksumForKeySql;
    }
}
