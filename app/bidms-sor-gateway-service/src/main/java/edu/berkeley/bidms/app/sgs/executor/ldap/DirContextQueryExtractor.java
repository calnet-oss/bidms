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

import edu.berkeley.bidms.app.sgs.config.properties.DirectoryAttributeConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.SorConfigProperties;
import edu.berkeley.bidms.app.sgs.executor.EntryContentExtractor;
import edu.berkeley.bidms.app.sgs.executor.EntryContentExtractorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextAdapter;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Extract query content from a directory context.
 */
public class DirContextQueryExtractor implements EntryContentExtractor<DirContextAdapter, LdapQueryEntryContent> {
    private final Logger log = LoggerFactory.getLogger(DirContextQueryExtractor.class);

    private String createTimestampAttributeName;
    private String modifyTimestampAttributeName;
    private Map<String, DirectoryAttributeConfigProperties> dirAttrMetadataMap;

    /**
     * @param createTimestampAttributeName The directory context attribute
     *                                     that contains the creation time
     *                                     for the entry.
     * @param modifyTimestampAttributeName The directory context attribute
     *                                     that contains the last modified
     *                                     time for the entry.
     * @param dirAttrMetadataMap           A map of {@link DirectoryAttributeConfigProperties}
     *                                     values where the map key is the
     *                                     attribute name.  This configures
     *                                     which attributes are multi-value.
     */
    public DirContextQueryExtractor(String createTimestampAttributeName, String modifyTimestampAttributeName, Map<String, DirectoryAttributeConfigProperties> dirAttrMetadataMap) {
        this.createTimestampAttributeName = createTimestampAttributeName;
        this.modifyTimestampAttributeName = modifyTimestampAttributeName;
        this.dirAttrMetadataMap = dirAttrMetadataMap;
    }

    /**
     * Extracts query content out of a {@link DirContextAdapter} and returns
     * a {@link LdapQueryEntryContent} instance.
     *
     * @param sorConfig A {@link SorConfigProperties} instance that contains
     *                  configuration for the SOR.
     * @param queryTime A {@link OffsetDateTime} instance that indicates when
     *                  the query was begun.
     * @param dirCtx    A {@link DirContextAdapter} instance that contains
     *                  the directory entry.
     * @return A {@link LdapQueryEntryContent}  instance contains the
     * extracted data.
     * @throws EntryContentExtractorException If there was an error
     *                                        extracting content of the
     *                                        entry.
     */
    @Override
    public LdapQueryEntryContent extractContent(SorConfigProperties sorConfig, OffsetDateTime queryTime, DirContextAdapter dirCtx) throws EntryContentExtractorException {
        Map<String, Object> map = new DirContextAdapterToMapConverter().convert(dirCtx);
        augmentMap(map);

        String sorObjKey = (String) map.get(getSorObjKeyAttributeName());
        if (sorObjKey == null) {
            throw new EntryContentExtractorException("Couldn't definitively determine " + getSorObjKeyAttributeName() + " for DN " + dirCtx.getDn());
        }

        return new LdapQueryEntryContent(sorConfig.getSorName(), sorObjKey, queryTime, map);
    }

    protected Map<String, DirectoryAttributeConfigProperties> getDirectoryAttrMetadataMap() {
        return dirAttrMetadataMap;
    }

    /**
     * Adds <code>definitiveUid</code> and converts multi-value attributes
     * into arrays regardless of whether there is only one value or not.
     * Configuration of multi-value attributes is done within the instance
     * returned by {@link #getDirectoryAttrMetadataMap()}.
     *
     * @param entryMap The LDAP directory context entry map, typically
     *                 generated by a {@link DirContextAdapterToMapConverter}.
     * @throws EntryContentExtractorException If there's a problem parsing
     *                                        data out of the entry,
     *                                        including not being able to
     *                                        come up with an unambiguous uid
     *                                        for the entry.
     */
    private void augmentMap(Map<String, Object> entryMap) throws EntryContentExtractorException {
        String dn = entryMap.get("dn").toString();

        // Get the "definitive" uid, if it can be determined.
        // The definitive uid is determined when there are no
        // ambiguities between DN and uid attribute. (Which should
        // hopefully always be the case!)
        DefinitiveUid definitiveUid = DefinitiveUid.getDefinitiveUid(entryMap);
        if (definitiveUid != null && definitiveUid.getDefinitiveUid() != null) {
            entryMap.put("definitiveUid", definitiveUid.getDefinitiveUid());
        } else {
            throw new EntryContentExtractorException("No definitiveUid for DN=" + dn);
        }

        // don't need the dnObject anymore since we just called
        // getDefinitiveUid
        entryMap.remove("dnObject");
        // don't want createTimestamp and modifyTimestamp
        entryMap.remove(createTimestampAttributeName);
        entryMap.remove(modifyTimestampAttributeName);

        // iterate each attribute returned and add it to the map
        for (String attrName : entryMap.keySet()) {
            DirectoryAttributeConfigProperties attrMetadata = getDirectoryAttrMetadataMap().get(attrName);
            if (attrMetadata == null) {
                // Not an attribute we have metadata for.  Leave alone
                // in the map, which means it'll still be there as-is.
                log.warn("Attribute " + attrName + " not in metadata map.  Don't know if it's supposed to be single or multi value.  Leaving as-is.");
                continue;
            }
            if (attrMetadata.getQuant() != DirectoryAttributeConfigProperties.QuantType.SINGLE) {
                // can be a multi-value attribute, force as an array in
                // the map if it isn't already
                Object attrVal = entryMap.get(attrName);
                if (!(attrVal instanceof Object[])) {
                    Object[] replacement = new Object[1];
                    replacement[0] = attrVal;
                    entryMap.put(attrName, replacement);
                }
            } else {
                // is a single-value attribute.  check if an array, log
                // a warning, and use the first array element as the
                // value.
                Object attrVal = entryMap.get(attrName);
                if (attrVal instanceof Object[]) {
                    log.warn(attrName + " is a multi-value attribute for uid=" + entryMap.get("uid") + " but it's configured as a single-value attribute");
                    entryMap.put(attrName, ((Object[]) attrVal)[0]);
                }
            }
        }
    }

    /**
     * @return The key in the augmented map that contains the primary key
     * value for the entry entry.
     */
    protected String getSorObjKeyAttributeName() {
        return "definitiveUid";
    }
}
