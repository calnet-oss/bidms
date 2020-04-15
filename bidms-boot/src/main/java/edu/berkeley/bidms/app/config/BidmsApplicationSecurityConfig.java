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
package edu.berkeley.bidms.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

@EnableWebSecurity
public class BidmsApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.datasource.driver-class-name}")
    private String dsDriverClassName;

    @Value("${spring.datasource.url}")
    private String dsUrl;

    @Value("${spring.datasource.username}")
    private String dsUsername;

    @Value("${spring.datasource.password}")
    private String dsPassword;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/hello/**").permitAll()
                .antMatchers("/sgs/*").hasAuthority("sorGateway")
                .antMatchers("/match-engine/*").hasAuthority("ucbMatch")
                .antMatchers("/match-service/*").hasAuthority("registryMatchService")
                /*.antMatchers("/**").denyAll()*/
                .anyRequest().denyAll()
                .and().httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(new SqlUserDetailsService(getDataSource()));
    }

    private DataSource getDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(dsDriverClassName)
                .url(dsUrl)
                .username(dsUsername)
                .password(dsPassword)
                .build();
    }

    static class SqlUserDetailsService implements UserDetailsService {
        private final Logger log = LoggerFactory.getLogger(SqlUserDetailsService.class);

        private DataSource dataSource;

        SqlUserDetailsService(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        static class SqlGrantedAuthority implements GrantedAuthority {
            private String authority;

            public SqlGrantedAuthority(String authority) {
                this.authority = authority;
            }

            @Override
            public String toString() {
                return "SqlGrantedAuthority{" +
                        "authority='" + authority + '\'' +
                        '}';
            }

            @Override
            public String getAuthority() {
                return authority;
            }
        }

        static class SqlUserDetails implements UserDetails {
            private Collection<SqlGrantedAuthority> authorities = new LinkedList<>();
            private String username;
            private String password;
            private boolean accountNonExpired;
            private boolean accountNonLocked;
            private boolean credentialsNonExpired;
            private boolean enabled;

            SqlUserDetails(String username, String password, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, boolean enabled) {
                this.username = username;
                this.password = password;
                this.accountNonExpired = accountNonExpired;
                this.accountNonLocked = accountNonLocked;
                this.credentialsNonExpired = credentialsNonExpired;
                this.enabled = enabled;
            }

            @Override
            public String toString() {
                return "SqlUserDetails{" +
                        "username='" + username + '\'' +
                        ", accountNonExpired=" + accountNonExpired +
                        ", accountNonLocked=" + accountNonLocked +
                        ", credentialsNonExpired=" + credentialsNonExpired +
                        ", enabled=" + enabled +
                        ", authorities=" + authorities +
                        '}';
            }

            @Override
            public Collection<SqlGrantedAuthority> getAuthorities() {
                return authorities;
            }

            public void setAuthorities(Collection<SqlGrantedAuthority> authorities) {
                this.authorities = authorities;
            }

            @Override
            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            @Override
            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            @Override
            public boolean isAccountNonExpired() {
                return accountNonExpired;
            }

            public void setAccountNonExpired(boolean accountNonExpired) {
                this.accountNonExpired = accountNonExpired;
            }

            @Override
            public boolean isAccountNonLocked() {
                return accountNonLocked;
            }

            public void setAccountNonLocked(boolean accountNonLocked) {
                this.accountNonLocked = accountNonLocked;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return credentialsNonExpired;
            }

            public void setCredentialsNonExpired(boolean credentialsNonExpired) {
                this.credentialsNonExpired = credentialsNonExpired;
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            try (Connection conn = dataSource.getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT id, username, passwordHash, accountExpired, accountLocked, passwordExpired, enabled FROM RegistryUser WHERE username = ?")) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // String username, String password, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, boolean enabled
                            boolean accountExpired = rs.getBoolean("accountExpired");
                            boolean accountLocked = rs.getBoolean("accountLocked");
                            boolean passwordExpired = rs.getBoolean("passwordExpired");
                            boolean enabled = rs.getBoolean("enabled");
                            SqlUserDetails userDetails = new SqlUserDetails(
                                    rs.getString("username"),
                                    rs.getString("passwordHash"), // Expected to be a bcrypt hash.  See PasswordEncoderSpec in bidms-spring-security-impl.
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
                                            userDetails.getAuthorities().add(new SqlGrantedAuthority(authority));
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
    }
}
