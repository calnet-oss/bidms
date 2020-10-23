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

import edu.berkeley.bidms.app.springsecurity.config.DownstreamProvisioningSecurityConfigurer;
import edu.berkeley.bidms.app.springsecurity.config.MatchEngineSecurityConfigurer;
import edu.berkeley.bidms.app.springsecurity.config.MatchServiceSecurityConfigurer;
import edu.berkeley.bidms.app.springsecurity.config.RegistryProvisioningSecurityConfigurer;
import edu.berkeley.bidms.app.springsecurity.config.RegistryServiceSecurityConfigurer;
import edu.berkeley.bidms.app.springsecurity.config.SgsSecurityConfigurer;
import edu.berkeley.bidms.app.springsecurity.service.RegistryUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class BidmsApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(BidmsApplicationSecurityConfig.class);

    @Autowired
    private RegistryUserDetailsService registryUserDetailsService;

    // === Optional beans that customize the security configuration for each service (app) ===
    // The app, if it wishes to override, can instantiate these beans in the app's @Configuration class.
    @Autowired(required = false)
    private SgsSecurityConfigurer sgsSecurityConfigurer;
    @Autowired(required = false)
    private MatchEngineSecurityConfigurer matchEngineSecurityConfigurer;
    @Autowired(required = false)
    private MatchServiceSecurityConfigurer matchServiceSecurityConfigurer;
    @Autowired(required = false)
    private RegistryProvisioningSecurityConfigurer registryProvisioningSecurityConfigurer;
    @Autowired(required = false)
    private DownstreamProvisioningSecurityConfigurer downstreamProvisioningSecurityConfigurer;
    @Autowired(required = false)
    private RegistryServiceSecurityConfigurer registryServiceSecurityConfigurer;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        var rm = http
                .csrf().disable()
                .authorizeRequests();

        rm.antMatchers("/hello/**").permitAll();

        if (sgsSecurityConfigurer != null) {
            log.info("SGS security rules being applied from " + sgsSecurityConfigurer.getClass().getName());
            sgsSecurityConfigurer.applyRules(rm);
        } else {
            log.info("SGS default security rules being applied");
            SgsSecurityConfigurer.defaultRules(rm);
        }

        if (matchEngineSecurityConfigurer != null) {
            log.info("Match engine security rules being applied from " + matchEngineSecurityConfigurer.getClass().getName());
            matchEngineSecurityConfigurer.applyRules(rm);
        } else {
            log.info("Match engine default security rules being applied");
            MatchEngineSecurityConfigurer.defaultRules(rm);
        }

        if (matchServiceSecurityConfigurer != null) {
            log.info("Match service security rules being applied from " + matchServiceSecurityConfigurer.getClass().getName());
            matchServiceSecurityConfigurer.applyRules(rm);
        } else {
            log.info("Match service default security rules being applied");
            MatchServiceSecurityConfigurer.defaultRules(rm);
        }

        if (registryProvisioningSecurityConfigurer != null) {
            log.info("Registry provisioning security rules being applied from " + registryProvisioningSecurityConfigurer.getClass().getName());
            registryProvisioningSecurityConfigurer.applyRules(rm);
        } else {
            log.info("Registry provisioning default security rules being applied");
            RegistryProvisioningSecurityConfigurer.defaultRules(rm);
        }

        if (downstreamProvisioningSecurityConfigurer != null) {
            log.info("Downstream provisioning security rules being applied from " + downstreamProvisioningSecurityConfigurer.getClass().getName());
            downstreamProvisioningSecurityConfigurer.applyRules(rm);
        } else {
            log.info("Downstream provisioning default security rules being applied");
            DownstreamProvisioningSecurityConfigurer.defaultRules(rm);
        }

        if (registryServiceSecurityConfigurer != null) {
            log.info("Registry service security rules being applied from " + registryServiceSecurityConfigurer.getClass().getName());
            registryServiceSecurityConfigurer.applyRules(rm);
        } else {
            log.info("Registry service default security rules being applied");
            RegistryServiceSecurityConfigurer.defaultRules(rm);
        }

        rm.anyRequest().denyAll()
                .and().httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(registryUserDetailsService);
    }
}
