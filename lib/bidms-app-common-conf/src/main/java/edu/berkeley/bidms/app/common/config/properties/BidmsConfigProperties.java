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
package edu.berkeley.bidms.app.common.config.properties;

import edu.berkeley.bidms.app.common.config.properties.rest.RestClientConfigProperties;
import edu.berkeley.bidms.app.common.config.properties.rest.RestProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Map;

@Validated
@Configuration
@ConfigurationProperties(prefix = "bidms")
public class BidmsConfigProperties {
    public static final String REST_CLIENT_KEY = "bidms.rest-client";
    private RestClientConfigProperties restClient;

    public static final String REST_KEY = "bidms.rest";
    @NotNull
    private RestProperties rest;

    public static final String JMS_CONNECTIONS_KEY = "bidms.jms-connections";
    private Map<String, JmsConnectionConfigProperties> jmsConnections;

    public void setRestClient(RestClientConfigProperties restClient) {
        this.restClient = restClient;
    }

    @Valid
    public RestProperties getRest() {
        return rest;
    }

    public void setRest(@Valid RestProperties rest) {
        this.rest = rest;
    }

    public URI getMatchEngineRestUrl() {
        return getRest().getMatchengine().getPerson().getUrl();
    }

    public URI getProvisionUidRestUrl() {
        return getRest().getProvision().getUid().getUrl();
    }

    public URI getProvisionNewUidRestUrl() {
        return getRest().getProvision().getNewUid().getUrl();
    }

    public Map<String, JmsConnectionConfigProperties> getJmsConnections() {
        return jmsConnections;
    }

    public void setJmsConnections(Map<String, JmsConnectionConfigProperties> jmsConnections) {
        this.jmsConnections = jmsConnections;
    }
}
