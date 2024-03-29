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
package edu.berkeley.bidms.app.matchservice.config;

import edu.berkeley.bidms.app.common.config.properties.BidmsConfigProperties;
import edu.berkeley.bidms.app.matchservice.config.properties.MatchServiceConfigProperties;
import edu.berkeley.bidms.app.matchservice.rest.MatchEngineRestTemplate;
import edu.berkeley.bidms.app.matchservice.rest.ProvisionRestTemplate;
import edu.berkeley.bidms.restclient.util.RestClientUtil;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class MatchServiceConfiguration {

    private BidmsConfigProperties bidmsConfigProperties;
    private MatchServiceConfigProperties matchServiceConfigProperties;

    public MatchServiceConfiguration(BidmsConfigProperties bidmsConfigProperties, MatchServiceConfigProperties matchServiceConfigProperties) {
        this.bidmsConfigProperties = bidmsConfigProperties;
        this.matchServiceConfigProperties = matchServiceConfigProperties;
    }

    private URI getRestMatchEngineBaseUrl() {
        return bidmsConfigProperties.getRestMatchEngineBaseUrl();
    }

    private String getMatchEngineRestUsername() {
        return matchServiceConfigProperties.getRest().getMatchengine().getUsername();
    }

    private String getMatchEngineRestPassword() {
        return matchServiceConfigProperties.getRest().getMatchengine().getPassword();
    }

    public URI getRestMatchEnginePersonUrl() {
        return bidmsConfigProperties.getRest().getMatchengine().getPerson().getUrl();
    }

    @Bean("matchServiceMatchEngineRestTemplate")
    public MatchEngineRestTemplate getMatchEngineRestTemplate(RestTemplateBuilder builder) {
        return RestClientUtil.configureSslBasicAuthRestTemplate(builder, getRestMatchEngineBaseUrl(), getMatchEngineRestUsername(), getMatchEngineRestPassword(), new MatchEngineRestTemplate());
    }

    private URI getRestProvisionBaseUrl() {
        return bidmsConfigProperties.getRestProvisionBaseUrl();
    }

    private String getProvisionRestUsername() {
        return matchServiceConfigProperties.getRest().getProvision().getUsername();
    }

    private String getProvisionRestPassword() {
        return matchServiceConfigProperties.getRest().getProvision().getPassword();
    }

    public URI getRestProvisionUidUrl() {
        return bidmsConfigProperties.getRest().getProvision().getUid().getUrl();
    }

    public URI getRestProvisionNewUidUrl() {
        return bidmsConfigProperties.getRest().getProvision().getNewUid().getUrl();
    }

    @Bean("matchServiceProvisionRestTemplate")
    public ProvisionRestTemplate getProvisionRestTemplate(RestTemplateBuilder builder) {
        return RestClientUtil.configureSslBasicAuthRestTemplate(builder, getRestProvisionBaseUrl(), getProvisionRestUsername(), getProvisionRestPassword(), new ProvisionRestTemplate());
    }
}
