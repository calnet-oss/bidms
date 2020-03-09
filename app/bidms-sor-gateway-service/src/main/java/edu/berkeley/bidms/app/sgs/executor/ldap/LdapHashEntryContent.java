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

import edu.berkeley.bidms.app.sgs.executor.HashEntryContent;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Hash data extracted from a directory context entry.
 */
public class LdapHashEntryContent extends HashEntryContent<Map<String, Object>> {

    /**
     * @param sorName        SOR name.
     * @param fullIdentifier Full unique identifier, which for LDAP is the DN
     *                       string.
     * @param sorObjKey      The primary key identifier, which for LDAP is
     *                       the uid.
     * @param hashTime       The time the hash query was executed.
     * @param nativeContent  The map of attribute values, typically generated
     *                       by {@link DirContextAdapterToMapConverter}.
     * @param timeMarker     The last modified time of the entry, or if that
     *                       is not present, the creation time.
     * @param numericMarker  A long integer, typically the timeMarker
     *                       converted to an epoch.
     * @param hash           The hash value of the entry.
     */
    public LdapHashEntryContent(
            String sorName,
            String fullIdentifier,
            String sorObjKey,
            OffsetDateTime hashTime,
            Map<String, Object> nativeContent,
            Timestamp timeMarker,
            long numericMarker,
            long hash
    ) {
        super(sorName, fullIdentifier, sorObjKey, hashTime, nativeContent, timeMarker, numericMarker, hash);
    }
}
