/*
 * Copyright (c) 2021, Regents of the University of California and
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
package edu.berkeley.bidms.springsecurity.util;

import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

public class ExtendedDelegatingPasswordEncoder extends DelegatingPasswordEncoder {

    /**
     * The default id used to lookup which {@link PasswordEncoder} should be
     * used for {@link #matches(CharSequence, String)} if the id is not
     * provided in the encoded password as a prefix.
     */
    private String defaultIdForDecode;

    /**
     * Creates a new instance
     *
     * @param idForEncode         the id used to lookup which {@link PasswordEncoder} should be
     *                            used for {@link #encode(CharSequence)}
     * @param idToPasswordEncoder a Map of id to {@link PasswordEncoder} used to determine
     *                            which {@link PasswordEncoder} should be used for
     *                            {@link #matches(CharSequence, String)}
     */
    public ExtendedDelegatingPasswordEncoder(String idForEncode, Map<String, PasswordEncoder> idToPasswordEncoder) {
        super(idForEncode, idToPasswordEncoder);
    }

    /**
     * Creates a new instance
     *
     * @param idForEncode         the id used to lookup which {@link PasswordEncoder} should be
     *                            used for {@link #encode(CharSequence)}
     * @param defaultIdForDecode  The default id used to lookup which {@link PasswordEncoder}
     *                            should be used for {@link #matches(CharSequence, String)} if
     *                            the id is not provided in the encoded password as a prefix
     * @param idToPasswordEncoder a Map of id to {@link PasswordEncoder} used to determine
     *                            which {@link PasswordEncoder} should be used for
     *                            {@link #matches(CharSequence, String)}
     */
    public ExtendedDelegatingPasswordEncoder(String idForEncode, String defaultIdForDecode, Map<String, PasswordEncoder> idToPasswordEncoder) {
        super(idForEncode, idToPasswordEncoder);
        this.defaultIdForDecode = defaultIdForDecode;
    }

    public String getDefaultIdForDecode() {
        return defaultIdForDecode;
    }

    public void setDefaultIdForDecode(String defaultIdForDecode) {
        this.defaultIdForDecode = defaultIdForDecode;
    }

    /**
     * When {@code defaultIdForDecode} is non-null AND
     * {@code prefixEncodedPassword} is lacking an id, then use
     * {@code defaultIdForDecode} for the password encoding type ID.
     * <p>
     * I.e, when {@code defaultIdForDecode} is non-null, if
     * {@code prefixEncodedPassword} is {@code password} rather
     * than {@code {id}password}, the super.matches() method will be
     * called as
     * {@code super.matches(rawPassword, "{"+getDefaultIdForDecode()+"}"+prefixEncodedPassword))}.
     * </p>
     */
    @Override
    public boolean matches(CharSequence rawPassword, String prefixEncodedPassword) {
        return super.matches(rawPassword, prefixEncodedPassword != null && getDefaultIdForDecode() != null && !prefixEncodedPassword.startsWith("{") ? "{" + getDefaultIdForDecode() + "}" + prefixEncodedPassword : prefixEncodedPassword);
    }
}
