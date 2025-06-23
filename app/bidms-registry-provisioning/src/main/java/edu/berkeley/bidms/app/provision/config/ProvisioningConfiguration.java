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
package edu.berkeley.bidms.app.provision.config;

import edu.berkeley.bidms.app.common.config.properties.BidmsConfigProperties;
import edu.berkeley.bidms.app.provision.config.properties.ProvisioningConfigProperties;
import edu.berkeley.bidms.provision.jms.DownstreamProvisionJmsTemplate;
import edu.berkeley.bidms.provision.jms.ProvisionJmsTemplate;
import edu.berkeley.bidms.provision.rest.DownstreamProvisioningRestTemplate;
import edu.berkeley.bidms.restclient.util.RestClientUtil;
import jakarta.jms.ConnectionFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProvisioningConfiguration {
    private BidmsConfigProperties bidmsConfigProperties;
    private ProvisioningConfigProperties provisioningConfigProperties;

    public ProvisioningConfiguration(
            BidmsConfigProperties bidmsConfigProperties,
            ProvisioningConfigProperties provisioningConfigProperties
    ) {
        this.bidmsConfigProperties = bidmsConfigProperties;
        this.provisioningConfigProperties = provisioningConfigProperties;
    }

    @Bean("provDownstreamProvisioningRestTemplate")
    public DownstreamProvisioningRestTemplate getDownstreamProvisioningRestTemplate(RestTemplateBuilder builder) {
        return RestClientUtil.configureSslBasicAuthRestTemplate(
                builder,
                bidmsConfigProperties.getRest().getDownstream().getBaseUrl(),
                provisioningConfigProperties.getRest().getDownstream().getUsername(),
                provisioningConfigProperties.getRest().getDownstream().getPassword(),
                new DownstreamProvisioningRestTemplate()
        );
    }

    @Bean
    public ProvisionJmsTemplate getProvisionJmsTemplate(ApplicationContext applicationContext) {
        return new ProvisionJmsTemplate(applicationContext.getBean(provisioningConfigProperties.getJms().getProvision().getJmsConnectionFactoryBeanName(), ConnectionFactory.class));
    }

    @Bean("provDownstreamProvisionJmsTemplate")
    public DownstreamProvisionJmsTemplate getDownstreamProvisionJmsTemplate(ApplicationContext applicationContext) {
        return new DownstreamProvisionJmsTemplate(applicationContext.getBean(provisioningConfigProperties.getJms().getDownstream().getJmsConnectionFactoryBeanName(), ConnectionFactory.class));
    }
}
