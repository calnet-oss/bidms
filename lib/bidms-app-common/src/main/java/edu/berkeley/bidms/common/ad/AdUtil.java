/*
 * Copyright (c) 2024, Regents of the University of California and
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
package edu.berkeley.bidms.common.ad;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Miscellaneous utility methods for handling Active Directory data.
 */
public abstract class AdUtil {

    /**
     * This converts a raw objectGUID byte array into a UUID.  The UUID in
     * string form is suitable for LDAP searches.  Using the OpenLDAP
     * {@code ldapsearch} command-line client, you can search using a
     * syntax similar to:
     * {@code ldapsearch -b "<GUID=UUID_STRING>"}
     *
     * @param g objectGUID bytes (should be 16 bytes)
     * @return A UUID object which, in string form, is suitable for LDAP searches
     */
    public static UUID convertObjectGUIDToUUID(byte[] g) {
        byte[] rearranged = new byte[]{
                g[3], g[2], g[1], g[0],
                g[5], g[4], g[7], g[6],
                g[8], g[9], g[10], g[11],
                g[12], g[13], g[14], g[15]
        };
        ByteBuffer bb = ByteBuffer.wrap(rearranged);
        return new UUID(bb.getLong(), bb.getLong());
    }
}
