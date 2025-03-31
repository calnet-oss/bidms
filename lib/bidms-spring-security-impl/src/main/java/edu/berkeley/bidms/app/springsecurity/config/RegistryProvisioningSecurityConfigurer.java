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
package edu.berkeley.bidms.app.springsecurity.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public interface RegistryProvisioningSecurityConfigurer extends ServiceSecurityConfigurer {

    @SuppressWarnings("UnusedReturnValue")
    static AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry defaultAuthorizeRequests(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry ar) {
        return ar.requestMatchers(AntPathRequestMatcher.antMatcher("/registry-provisioning/**")).hasAuthority("registryProvisioning");
    }

    @SuppressWarnings("UnusedReturnValue")
    static HttpBasicConfigurer<HttpSecurity> defaultHttpBasic(HttpBasicConfigurer<HttpSecurity> httpBasic) {
        return httpBasic.realmName("Registry Realm");
    }

    static HttpSecurity defaultRules(HttpSecurity http) throws Exception {
        return http.securityMatchers((sm) -> {
                    sm.requestMatchers(AntPathRequestMatcher.antMatcher("/registry-provisioning/**"));
                })
                .authorizeHttpRequests(RegistryProvisioningSecurityConfigurer::defaultAuthorizeRequests)
                .httpBasic(RegistryProvisioningSecurityConfigurer::defaultHttpBasic);
    }

    @Override
    default HttpSecurity applyRules(HttpSecurity http) throws Exception {
        return defaultRules(http);
    }
}
