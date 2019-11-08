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
package edu.berkeley.bidms.springsecurity.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * An implementation of Spring's {@link UserDetails} built from a BIDMS
 * RegistryUser object during authentication.
 */
public class RegistryUserDetails implements UserDetails {
    private String username;
    private boolean useDigestAuth;
    private String password;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    private boolean accountNonLocked;
    private Collection<? extends GrantedAuthority> authorities;
    private Long salt;

    /**
     * Construct by cloning data from an existing {@link UserDetails} object
     * and by augmenting it using additional registry data.
     *
     * @param userDetails   An existing {@link UserDetails} object to clone
     *                      data from.
     * @param useDigestAuth True if using HTTP digest authentication.
     * @param salt          The salt used for generating a password hash.
     * @param password      The password hash.
     */
    public RegistryUserDetails(UserDetails userDetails, Boolean useDigestAuth, Long salt, String password) {
        this.username = userDetails.getUsername();
        this.useDigestAuth = useDigestAuth;
        this.password = password; // httpDigestHash if useDigestAuth
        this.enabled = userDetails.isEnabled();
        this.accountNonExpired = userDetails.isAccountNonExpired();
        this.credentialsNonExpired = userDetails.isCredentialsNonExpired();
        this.accountNonLocked = userDetails.isAccountNonLocked();
        this.authorities = userDetails.getAuthorities();
        this.salt = salt;
    }

    /**
     * See {@link UserDetails#getUsername()}.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Using HTTP Digest authentication?
     *
     * @return True if using HTTP digest authentication.
     */
    public boolean isUseDigestAuth() {
        return useDigestAuth;
    }

    /**
     * See {@link UserDetails#getPassword()}.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * See {@link UserDetails#isEnabled()} ()}.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * See {@link UserDetails#isAccountNonExpired()}.
     */
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    /**
     * See {@link UserDetails#isCredentialsNonExpired()}.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    /**
     * See {@link UserDetails#isAccountNonLocked()}.
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * See {@link UserDetails#getAuthorities()}.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Get the salt used for generating a password hash.
     *
     * @return The salt used for generating a password hash.
     */
    public Long getSalt() {
        return salt;
    }
}
