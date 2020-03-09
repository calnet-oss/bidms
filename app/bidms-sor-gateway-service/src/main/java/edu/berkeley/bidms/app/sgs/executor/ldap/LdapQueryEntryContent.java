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

import edu.berkeley.bidms.app.sgs.converter.MapToJsonStringConverter;
import edu.berkeley.bidms.app.sgs.executor.QueryEntryContent;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Query data extracted from a directory context entry.
 */
public class LdapQueryEntryContent extends QueryEntryContent<Map<String, Object>> {

    /**
     * @param sorName       SOR name.
     * @param sorObjKey     The primary key identifier, which for LDAP is the
     *                      uid.
     * @param queryTime     The time the query was executed.
     * @param nativeContent The map of attribute values, typically generated
     *                      by {@link DirContextAdapterToMapConverter}.
     */
    public LdapQueryEntryContent(String sorName, String sorObjKey, OffsetDateTime queryTime, Map<String, Object> nativeContent) {
        super(sorName, sorObjKey, queryTime, nativeContent, new MapToJsonStringConverter());
    }

    /**
     * @return The full unique identifier, which for LDAP is the DN string.
     */
    public String getFullIdentifier() {
        return (String) getNativeContent().get("dn");
    }
}
