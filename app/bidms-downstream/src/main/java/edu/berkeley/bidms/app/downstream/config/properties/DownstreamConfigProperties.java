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
package edu.berkeley.bidms.app.downstream.config.properties;

import edu.berkeley.bidms.app.downstream.config.properties.jms.JmsProperties;
import edu.berkeley.bidms.app.downstream.config.properties.job.JobConfigProperties;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(prefix = "bidms.downstream")
public class DownstreamConfigProperties {

    private DirectoryConnectionConfigProperties ldap;
    private DirectoryConnectionConfigProperties ad;
    private LdapConnectorConfigProperties ldapConnector;
    private JobConfigProperties job;
    @NotNull
    private JmsProperties jms;

    public DirectoryConnectionConfigProperties getLdap() {
        return ldap;
    }

    public void setLdap(DirectoryConnectionConfigProperties ldap) {
        this.ldap = ldap;
    }

    public DirectoryConnectionConfigProperties getAd() {
        return ad;
    }

    public void setAd(DirectoryConnectionConfigProperties ad) {
        this.ad = ad;
    }

    public LdapConnectorConfigProperties getLdapConnector() {
        return ldapConnector;
    }

    public void setLdapConnector(LdapConnectorConfigProperties ldapConnector) {
        this.ldapConnector = ldapConnector;
    }

    public JobConfigProperties getJob() {
        return job;
    }

    public void setJob(JobConfigProperties job) {
        this.job = job;
    }

    public void setJms(JmsProperties jms) {
        this.jms = jms;
    }

    public JmsProperties getJms() {
        return jms;
    }
}
