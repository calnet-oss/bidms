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
package edu.berkeley.bidms.app.springsecurity.service;

import edu.berkeley.bidms.springsecurity.user.RegistryUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class RegistryUserDetailsService implements UserDetailsService {
    private final Logger log = LoggerFactory.getLogger(RegistryUserDetailsService.class);

    private final DataSource dataSource;

    /**
     * By default (the common case), this is expected to be a bcrypt hash to
     * be used for Basic Auth.  See PasswordEncoderSpec in
     * bidms-spring-security-impl.
     * <p>
     * Alternatively (the rare case), it can be something else like a
     * Digest Auth hash where this UserDetailsService is used by a
     * DigestAuthenticationFilter. (Not recommended.)
     */
    private String passwordColumnName = "passwordHash";

    public RegistryUserDetailsService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try (Connection conn = getDataSource().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT id, username, " + passwordColumnName + ", accountExpired, accountLocked, passwordExpired, enabled FROM RegistryUser WHERE username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // String username, String password, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, boolean enabled
                        boolean accountExpired = rs.getBoolean("accountExpired");
                        boolean accountLocked = rs.getBoolean("accountLocked");
                        boolean passwordExpired = rs.getBoolean("passwordExpired");
                        boolean enabled = rs.getBoolean("enabled");
                        RegistryUserDetails userDetails = new RegistryUserDetails(
                                rs.getString("username"),
                                rs.getString(passwordColumnName), // Usually expected to be a bcrypt hash.  See PasswordEncoderSpec in bidms-spring-security-impl.
                                !accountExpired,
                                !accountLocked,
                                !passwordExpired,
                                rs.getBoolean("enabled")
                        );
                        if (enabled && !accountExpired && !accountLocked && !passwordExpired) {
                            try (PreparedStatement ps2 = conn.prepareStatement("SELECT rr.authority FROM RegistryUserRole rur, RegistryRole rr WHERE rur.registryUserId = ? AND rr.id = rur.registryRoleId")) {
                                ps2.setLong(1, rs.getLong("id"));
                                try (ResultSet rs2 = ps2.executeQuery()) {
                                    while (rs2.next()) {
                                        String authority = rs2.getString("authority");
                                        userDetails.getAuthorities().add(new RegistryUserDetails.RegistryUserDetailsGrantedAuthority(authority));
                                    }
                                }
                            }
                        }
                        log.debug("userDetails=" + userDetails);
                        return userDetails;
                    } else {
                        throw new UsernameNotFoundException("Username not found");
                    }
                }
            }
        } catch (SQLException | RuntimeException e) {
            log.error("Unexpected error occurred trying to find registry user", e);
            throw new UsernameNotFoundException("Unexpected error occurred trying to find registry user", e);
        }
    }

    public String getPasswordColumnName() {
        return passwordColumnName;
    }

    public void setPasswordColumnName(String passwordColumnName) {
        this.passwordColumnName = passwordColumnName;
    }
}
