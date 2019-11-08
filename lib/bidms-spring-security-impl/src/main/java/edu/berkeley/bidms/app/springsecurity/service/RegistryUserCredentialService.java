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
package edu.berkeley.bidms.app.springsecurity.service;

import edu.berkeley.bidms.app.springsecurity.encoder.DigestAuthPasswordEncoder;
import edu.berkeley.bidms.springsecurity.api.service.CredentialSetter;
import edu.berkeley.bidms.springsecurity.api.user.CredentialsAwareUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * A service to set credentials for objects that implement the {@link
 * CredentialsAwareUser} interface.
 */
@Service
public class RegistryUserCredentialService implements CredentialSetter {
    private PasswordEncoder passwordEncoder;
    private DigestAuthPasswordEncoder httpDigestPasswordEncoder;

    /**
     * Get the {@link PasswordEncoder} instance being used by the service.
     *
     * @return The {@link PasswordEncoder} instance being used by the
     * service.
     */
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    /**
     * Set the {@link PasswordEncoder} instance used by the service.
     *
     * @param passwordEncoder The {@link PasswordEncoder} instance.
     */
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get the {@link DigestAuthPasswordEncoder} instance being used by the
     * service.
     *
     * @return The {@link DigestAuthPasswordEncoder} instance being used by
     * the service.
     */
    public DigestAuthPasswordEncoder getHttpDigestPasswordEncoder() {
        return httpDigestPasswordEncoder;
    }

    /**
     * Set the {@link DigestAuthPasswordEncoder} instance used by the
     * service.
     *
     * @param httpDigestPasswordEncoder The {@link DigestAuthPasswordEncoder}
     *                                  instance.
     */
    public void setHttpDigestPasswordEncoder(DigestAuthPasswordEncoder httpDigestPasswordEncoder) {
        this.httpDigestPasswordEncoder = httpDigestPasswordEncoder;
    }

    private String generatePasswordHash(String rawPassword) {
        // Stronger hash.  This can used for authentication outside of
        // http digest authentication.
        return passwordEncoder.encode(rawPassword);
    }

    private String generatePasswordHttpDigestHash(String username, String password) {
        // This version is MD5 with the input string in the format used by
        // Http Digest Authentication.  This is used when
        // spring-security is configured with useDigestAuth=true.
        return httpDigestPasswordEncoder.encode("{" + username + "}" + password);
    }

    /**
     * Set a password for a {@link CredentialsAwareUser} object.
     *
     * @param credentialsAwareUser The {@link CredentialsAwareUser} object.
     * @param password             The password to set.
     */
    public void setPassword(CredentialsAwareUser credentialsAwareUser, String password) {
        if (credentialsAwareUser == null) {
            throw new IllegalArgumentException("registryUser can't be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("password can't be null");
        }
        if (passwordEncoder == null) {
            throw new RuntimeException("passwordEncoder hasn't been set");
        }
        if (httpDigestPasswordEncoder == null) {
            throw new RuntimeException("httpDigestPasswordEncoder hasn't been set");
        }
        String passwordHash = generatePasswordHash(password);
        credentialsAwareUser.setPasswordHash(passwordHash);
        credentialsAwareUser.setPasswordHttpDigestHash(generatePasswordHttpDigestHash(credentialsAwareUser.getUsername(), password));
    }
}
