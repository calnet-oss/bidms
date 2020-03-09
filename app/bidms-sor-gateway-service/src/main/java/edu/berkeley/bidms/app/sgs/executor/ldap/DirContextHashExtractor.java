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
package edu.berkeley.bidms.app.sgs.executor.ldap;

import edu.berkeley.bidms.app.sgs.config.properties.LdapConnectionConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.SorConfigProperties;
import edu.berkeley.bidms.app.sgs.executor.EntryContentExtractor;
import edu.berkeley.bidms.app.sgs.executor.EntryContentExtractorException;
import edu.berkeley.bidms.app.sgs.executor.ExecutorUtil;
import org.springframework.ldap.core.DirContextAdapter;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * Extract hash content from a directory context.
 */
public class DirContextHashExtractor implements EntryContentExtractor<DirContextAdapter, LdapHashEntryContent> {
    private LdapConnectionConfigProperties.LdapDialect ldapDialect;
    private String createTimestampAttributeName;
    private String modifyTimestampAttributeName;

    /**
     * @param ldapDialect                  The type of directory server.
     * @param createTimestampAttributeName The directory context attribute
     *                                     that contains the creation time
     *                                     for the entry.
     * @param modifyTimestampAttributeName The directory context attribute
     *                                     that contains the last modified
     *                                     time for the entry.
     */
    public DirContextHashExtractor(LdapConnectionConfigProperties.LdapDialect ldapDialect, String createTimestampAttributeName, String modifyTimestampAttributeName) {
        this.ldapDialect = ldapDialect;
        this.createTimestampAttributeName = createTimestampAttributeName;
        this.modifyTimestampAttributeName = modifyTimestampAttributeName;
    }

    /**
     * Extracts query content out of a {@link DirContextAdapter} and returns
     * a {@link LdapHashEntryContent} instance.
     *
     * @param sorConfig A {@link SorConfigProperties} instance that contains
     *                  configuration for the SOR.
     * @param hashTime  A {@link OffsetDateTime} instance that indicates when
     *                  the hash query was begun.
     * @param dirCtx    A {@link DirContextAdapter} instance that contains
     *                  the directory entry.
     * @return A {@link LdapHashEntryContent} instance contains the extracted
     * data.
     * @throws EntryContentExtractorException If there was an error
     *                                        extracting content of the
     *                                        entry.
     */
    @Override
    public LdapHashEntryContent extractContent(SorConfigProperties sorConfig, OffsetDateTime hashTime, DirContextAdapter dirCtx) throws EntryContentExtractorException {
        Map<String, Object> entryMap = new DirContextAdapterToMapConverter().convert(dirCtx);

        // Get the "definitive" uid, if it can be determined.
        // The definitive uid is determined when there are no
        // ambiguities between DN and uid attribute. (Which should
        // hopefully always be the case!)
        DefinitiveUid definitiveUid = DefinitiveUid.getDefinitiveUid(entryMap);
        if (definitiveUid == null || definitiveUid.getDefinitiveUid() == null) {
            throw new EntryContentExtractorException("No definitiveUid for DN=" + dirCtx.getDn());
        }

        String fullIdentifier = dirCtx.getDn().toString();
        String sorObjKey = definitiveUid.getDefinitiveUid();

        Timestamp timeMarker = null;
        try {
            if (entryMap.containsKey(modifyTimestampAttributeName)) {
                timeMarker = new Timestamp(convertLdapTimestamp((String) entryMap.get(modifyTimestampAttributeName)).getTime());
            } else if (entryMap.containsKey(createTimestampAttributeName)) {
                timeMarker = new Timestamp(convertLdapTimestamp((String) entryMap.get(createTimestampAttributeName)).getTime());
            } else if (sorConfig.isHashQueryTimestampSupported()) {
                throw new EntryContentExtractorException("Neither " + modifyTimestampAttributeName + " nor " + createTimestampAttributeName + " in " + dirCtx.getDn() + ".  At least one of them is required.");
            }
        } catch (ParseException e) {
            throw new EntryContentExtractorException(e);
        }

        // We don't want to hash modifyTimestamp or
        // createTimestamp, as the whole point is to find out if
        // only the other attributes we care about have changed.
        // So remove it from the map before it's hashed.
        entryMap.remove(createTimestampAttributeName);
        entryMap.remove(modifyTimestampAttributeName);

        // Also don't want to include dnObject, as this made redundant by equivalent "dn" string.
        entryMap.remove("dnObject");

        long hash = entryMap.toString().hashCode();

        return new LdapHashEntryContent(
                sorConfig.getSorName(),
                fullIdentifier,
                sorObjKey,
                hashTime,
                entryMap,
                timeMarker,
                timeMarker != null ? ExecutorUtil.convertTimestampToMicroseconds(timeMarker) : null,
                hash
        );
    }

    /**
     * Convert timestamp strings from LDAP to Date objects.
     */
    @SuppressWarnings("all")
    private Date convertLdapTimestamp(String ldapTimestamp) throws ParseException {
        String timeFormat;
        boolean isUTC;
        switch (ldapDialect) {
            case APACHEDS:
                timeFormat = "yyyyMMddHHmmss.sss'Z'";
                isUTC = true;
                break;
            default:
                timeFormat = "yyyyMMddHHmmss'Z'";
                isUTC = true;
        }
        SimpleDateFormat ldapSdf = new SimpleDateFormat(timeFormat);
        // LDAP timestamps are usually in UTC/GMT
        if (isUTC) {
            ldapSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        return ldapTimestamp != null ? ldapSdf.parse(ldapTimestamp) : null;
    }
}
