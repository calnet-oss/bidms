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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Name;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class that contains values relating to the determination of a
 * "definitive uid" for a LDAP entry. Upon successful calculation, there is a
 * {@link #getDefinitiveUid()} non-null value.  Otherwise, at least one error
 * flag will indicate true.
 */
public class DefinitiveUid {
    private static final Logger log = LoggerFactory.getLogger(DefinitiveUid.class);

    private String definitiveUid;
    private boolean uidMissingFromDN;
    private boolean uidMissingAsAttribute;
    private boolean uidMultipleAttributeValues;
    private boolean uidsDontMatchWithDN;

    /**
     * @return A string that contains the value of all the error flags.
     * Useful for logging an error.
     */
    public String getErrorIndicatorsString() {
        return "uidMissingFromDN=" + uidMissingFromDN + ", uidMissingAsAttribute=" + uidMissingAsAttribute + ", uidMultipleAttributeValues=" + uidMultipleAttributeValues + ", uidsDontMatchWithDN=" + uidsDontMatchWithDN;
    }

    /**
     * @return A non-null value indicates un unambiguous uid was successfully
     * determined.
     */
    public String getDefinitiveUid() {
        return definitiveUid;
    }

    public void setDefinitiveUid(String definitiveUid) {
        this.definitiveUid = definitiveUid;
    }

    /**
     * @return true when there is no <code>uid</code> attribute in the DN.
     */
    public boolean isUidMissingFromDN() {
        return uidMissingFromDN;
    }

    public void setUidMissingFromDN(boolean uidMissingFromDN) {
        this.uidMissingFromDN = uidMissingFromDN;
    }

    /**
     * @return true when there is no <code>uid</code> attribute in the LDAP
     * entry.
     */
    public boolean isUidMissingAsAttribute() {
        return uidMissingAsAttribute;
    }

    public void setUidMissingAsAttribute(boolean uidMissingAsAttribute) {
        this.uidMissingAsAttribute = uidMissingAsAttribute;
    }

    /**
     * @return true when there are multiple <code>uid</code> values within
     * the LDAP entry.
     */
    public boolean isUidMultipleAttributeValues() {
        return uidMultipleAttributeValues;
    }

    public void setUidMultipleAttributeValues(boolean uidMultipleAttributeValues) {
        this.uidMultipleAttributeValues = uidMultipleAttributeValues;
    }

    /**
     * @return true when the <code>uid</code> attribute in the LDAP entry
     * does not match the uid in the entry DN.
     */
    public boolean isUidsDontMatchWithDN() {
        return uidsDontMatchWithDN;
    }

    public void setUidsDontMatchWithDN(boolean uidsDontMatchWithDN) {
        this.uidsDontMatchWithDN = uidsDontMatchWithDN;
    }

    /**
     * Get the "definitive" uid from a LDAP entry.
     * <p>
     * Requires that the <code>dnObject</code> key has been added to the
     * map.
     * <p>
     * If successful, {@link #getDefinitiveUid()} will contain a non-null
     * value.  If not successful, any of the following error flags may exist
     * in the result to indicate why the definitive uid could not be
     * determined:
     * <ul>
     *     <li>{@link #isUidMissingFromDN()}</li>
     *     <li>{@link #isUidMissingAsAttribute()}</li>
     *     <li>{@link #isUidMultipleAttributeValues()}</li>
     *     <li>{@link #isUidsDontMatchWithDN()}</li>
     * </ul>
     *
     * @param entryMap The LDAP entry attribute value map.
     * @return A {@link DefinitiveUid} object that indicates what was
     * determined in regards to a UID for the LDAP entry.
     */
    public static DefinitiveUid getDefinitiveUid(Map<String, Object> entryMap) {
        DefinitiveUid result = new DefinitiveUid();
        // Since uid is technically a multi-value attribute, lets try and
        // determine a definitive uid.  This is mostly just sanity-checking
        // and/or error reporting.  Obviously, all our records SHOULD only
        // have one uid attribute value and the uid attribute value should
        // also of course match the uid in the DN.  If those two conditions
        // aren't met, then there's a problem with the record.
        String dn = entryMap.get("dn").toString();
        // get the uid from the DN
        String uidFromDN = extractUid(entryMap);
        if (uidFromDN == null) {
            log.warn("Unable to determine uid from dn " + dn);
            result.setUidMissingFromDN(true);
        }

        // check how many uid attribute values the record has
        Object uidAttr = entryMap.get("uid");
        if (uidAttr == null) {
            log.warn(dn + " doesn't seem to have a uid attribute value");
            result.setUidMissingAsAttribute(true);
        } else if (uidAttr instanceof Object[] && Arrays.stream((Object[]) uidAttr).distinct().count() > 1) {
            if (((Object[]) uidAttr).length > 1) {
                log.warn(dn + " seems to have " + ((Object[]) uidAttr).length + " uid attribute values");
                result.setUidMultipleAttributeValues(true);
            }
        } else if (uidFromDN != null) {
            // if we have a uid from DN and we have at least one uid
            // attribute value, we have the potential to determine the uid
            // definitvely, as long as they all match as the same uid

            // lets make sure the uid attribute values are the same as
            // what's in the DN
            boolean uidDifference = false;
            if (uidAttr instanceof Object[]) {
                for (Object uidValue : (Object[]) uidAttr) {
                    uidValue = uidValue != null ? uidValue.toString().trim() : null;
                    if (!uidValue.equals(uidFromDN)) {
                        log.warn("uid attribute value " + uidValue + " does not match uid from DN: " + uidFromDN);
                        uidDifference = true;
                        result.setUidsDontMatchWithDN(true);
                        break;
                    }
                }
            } else {
                uidAttr = uidAttr != null ? uidAttr.toString().trim() : null;
                if (!uidAttr.equals(uidFromDN)) {
                    log.warn("uid attribute value " + uidAttr + " does not match uid from DN: " + uidFromDN);
                    uidDifference = true;
                    result.setUidsDontMatchWithDN(true);
                }
            }

            // if no difference in uids, we have determined the uid
            // definitively
            if (!uidDifference) {
                result.setDefinitiveUid(uidFromDN);
            }
        }

        return result;
    }

    private static String extractUid(Map<String, Object> ldapObj) {
        return extractUid((Name) ldapObj.get("dnObject"));
    }

    private static String extractUid(Name dnObject) {
        // get() seems to index in reverse
        for (int i = dnObject.size() - 1; i >= 0; i--) {
            if (dnObject.get(i).toLowerCase().startsWith("uid=")) {
                return dnObject.get(i).substring("uid=".length()).trim();
            }
        }
        return null;
    }
}
