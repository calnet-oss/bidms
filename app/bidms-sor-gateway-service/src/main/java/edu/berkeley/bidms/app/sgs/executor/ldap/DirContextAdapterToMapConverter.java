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

import edu.berkeley.bidms.app.sgs.converter.ConversionErrorException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.ldap.core.DirContextAdapter;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import java.util.Map;
import java.util.TreeMap;

/**
 * Converts a {@link DirContextAdapter} to a {@link Map}.
 */
public class DirContextAdapterToMapConverter implements Converter<DirContextAdapter, Map<String, Object>> {
    /**
     * Converts a {@link DirContextAdapter} to a {@link Map}.  Multi-value
     * attributes within the context are represented as array values within
     * the map.
     * <p>
     * The map is augmented with two extra key/value pairs that aren't normal
     * directory context attributes:
     * <ul>
     *     <li><code>dn</code> - The distinguished name of the context, as a {@link String}.</li>
     *     <li><code>dnObject</code> - The distinguished name of the context, as a {@link javax.naming.Name}.</li>
     * </ul>
     *
     * @param ctx The {@link DirContextAdapter} to convert.
     * @return A {@link Map}, converted from the context.
     */
    @Override
    public Map<String, Object> convert(DirContextAdapter ctx) {
        // We want our map keys to be sorted so we have the same
        // determinant key ordering if the data is the same.
        TreeMap<String, Object> p = new TreeMap<>();

        // put the dn in the map
        p.put("dn", ctx.getDn().toString());
        p.put("dnObject", ctx.getDn());

        // iterate each attribute returned and add it to the map
        try {
            var attributes = ctx.getAttributes().getAll();
            try {
                while (attributes.hasMore()) {
                    Attribute attr = attributes.next();
                    if (attr.size() > 1) {
                        // multiple values
                        Object[] values = new Object[attr.size()];
                        for (int i = 0; i < values.length; i++) {
                            values[i] = attr.get(i);
                        }
                        p.put(attr.getID(), values);
                    } else if (attr.size() == 1) {
                        // single value
                        p.put(attr.getID(), attr.get());
                    }
                }
            } finally {
                attributes.close();
            }
        } catch (NamingException e) {
            throw new ConversionErrorException("Couldn't convert LDAP DirContext entry to a map", e);
        }

        return p;
    }
}
