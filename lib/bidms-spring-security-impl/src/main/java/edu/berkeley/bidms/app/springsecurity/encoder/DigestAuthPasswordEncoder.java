/*
 * Copyright (c) 2019, Regents of the University of California and
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
package edu.berkeley.bidms.app.springsecurity.encoder;

import edu.berkeley.bidms.springsecurity.util.RegistryDigestUtil;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A password encoder to generate hash values suitable for HTTP Digest
 * authentication.
 */
public class DigestAuthPasswordEncoder implements PasswordEncoder {
    public static final String DEFAULT_REALM = "Registry Realm";

    private String realm;

    public DigestAuthPasswordEncoder() {
        this(DEFAULT_REALM);
    }

    public DigestAuthPasswordEncoder(String realm) {
        this.realm = realm;
    }

    public String getRealm() {
        return realm;
    }

    @Override
    public String encode(CharSequence rawPass) {
        String username;
        String password;
        String[] saltAndPassword = RegistryDigestUtil.extractSaltAndPassword(rawPass.toString());
        String salt = saltAndPassword[0];
        password = saltAndPassword[1];
        if (salt == null) {
            throw new RuntimeException("Salt is required and must be the username.  Pass in a string in the format of '{username}password'.");
        }
        username = salt;

        return md5Hex(username + ':' + getRealm() + ':' + password);
    }

    @Override
    public boolean matches(CharSequence rawPass, String encPass) {
        // the 'raw' password will already be in hashed form, so compare directly
        return (encPass != null && rawPass != null) && rawPass.toString().equals(encPass);
    }

    public static String md5Hex(String s) {
        try {
            return new String(Hex.encode(MessageDigest.getInstance("MD5").digest(s.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }
    }

    // derived from Spring Security's DigestAuthUtils class (expects password to already be encoded)
    public static String generateDigest(
            String encodedPassword, String httpMethod,
            String uri, String qop, String nonce, String nc, String cnonce
    )
            throws IllegalArgumentException {
        String a2 = httpMethod + ":" + uri;
        String a1Md5 = encodedPassword;
        String a2Md5 = md5Hex(a2);
        if (qop == null) {
            // as per RFC 2069 compliant clients (also reaffirmed by RFC 2617)
            return md5Hex(a1Md5 + ":" + nonce + ":" + a2Md5);
        }
        if ("auth".equals(qop)) {
            // As per RFC 2617 compliant clients
            return md5Hex(a1Md5 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + a2Md5);
        }
        throw new IllegalArgumentException("This method does not support a qop: '" + qop + "'");
    }
}
