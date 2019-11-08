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
package edu.berkeley.bidms.springsecurity.api.user;

/**
 * An object that has a secure password hash and a less secure hash suitable
 * for HTTP Digest authentication.
 */
public interface CredentialsAwareUser {
    /**
     * Get the user's username.
     *
     * @return The user's username.
     */
    String getUsername();

    /**
     * Get the secure password hash.
     *
     * @return The secure password hash.
     */
    String getPasswordHash();

    /**
     * Set the secure password hash.
     *
     * @param passwordHash The secure password hash.
     */
    void setPasswordHash(String passwordHash);

    /**
     * Get the HTTP Digest auth hash.
     *
     * @return The HTTP Digest auth hash.
     */
    String getPasswordHttpDigestHash();

    /**
     * Set the HTTP Digest auth hash.
     *
     * @param passwordHttpDigestHash The HTTP Digest auth hash.
     */
    void setPasswordHttpDigestHash(String passwordHttpDigestHash);
}
