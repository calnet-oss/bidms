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
import edu.berkeley.bidms.app.common.config.properties.JmsConnectionConfigProperties;
import edu.berkeley.bidms.app.matchservice.config.properties.MatchServiceConfigProperties;
import edu.berkeley.bidms.app.matchservice.jms.ProvisionJmsTemplate;
import edu.berkeley.bidms.app.matchservice.rest.MatchEngineRestTemplate;
import edu.berkeley.bidms.app.matchservice.rest.ProvisionRestTemplate;
import edu.berkeley.bidms.common.rest.RestClientUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

import javax.jms.ConnectionFactory;
import java.net.URI;

@SpringBootConfiguration
public class MatchServiceConfiguration {

    private BidmsConfigProperties bidmsConfigProperties;
    private MatchServiceConfigProperties matchServiceConfigProperties;

    public MatchServiceConfiguration(BidmsConfigProperties bidmsConfigProperties, MatchServiceConfigProperties matchServiceConfigProperties) {
        this.bidmsConfigProperties = bidmsConfigProperties;
        this.matchServiceConfigProperties = matchServiceConfigProperties;
    }

    public URI getMatchEngineRestUrl() {
        if (bidmsConfigProperties.getRest() == null
                || !bidmsConfigProperties.getRest().containsKey("matchengine")
                || !bidmsConfigProperties.getRest().get("matchengine").containsKey("person")
                || bidmsConfigProperties.getRest().get("matchengine").get("person").getUrl() == null) {
            throw new RuntimeException(BidmsConfigProperties.REST_KEY + ".matchengine.person.url is not configured");
        }
        return bidmsConfigProperties.getRest().get("matchengine").get("person").getUrl();
    }

    private String getMatchEngineRestUsername() {
        if (matchServiceConfigProperties.getRest() == null ||
                !matchServiceConfigProperties.getRest().containsKey("matchengine") ||
                matchServiceConfigProperties.getRest().get("matchengine").getUsername() == null) {
            throw new RuntimeException(MatchServiceConfigProperties.REST_KEY + ".matchengine.username is not configured");
        }
        return matchServiceConfigProperties.getRest().get("matchengine").getUsername();
    }

    private String getMatchEngineRestPassword() {
        if (matchServiceConfigProperties.getRest() == null ||
                !matchServiceConfigProperties.getRest().containsKey("matchengine") ||
                matchServiceConfigProperties.getRest().get("matchengine").getPassword() == null) {
            throw new RuntimeException(MatchServiceConfigProperties.REST_KEY + ".matchengine.password is not configured");
        }
        return matchServiceConfigProperties.getRest().get("matchengine").getPassword();
    }

    @Bean
    public MatchEngineRestTemplate getMatchEngineRestTemplate(RestTemplateBuilder builder) {
        return RestClientUtil.configureSslBasicAuthRestTemplate(builder, getMatchEngineRestUrl(), getMatchEngineRestUsername(), getMatchEngineRestPassword(), new MatchEngineRestTemplate());
    }

    public URI getProvisionUidRestUrl() {
        if (bidmsConfigProperties.getRest() == null
                || !bidmsConfigProperties.getRest().containsKey("provision")
                || !bidmsConfigProperties.getRest().get("provision").containsKey("uid")
                || bidmsConfigProperties.getRest().get("provision").get("uid").getUrl() == null) {
            throw new RuntimeException(BidmsConfigProperties.REST_KEY + ".provision.uid.url is not configured");
        }
        return bidmsConfigProperties.getRest().get("provision").get("uid").getUrl();
    }

    public URI getProvisionNewUidRestUrl() {
        if (bidmsConfigProperties.getRest() == null
                || !bidmsConfigProperties.getRest().containsKey("provision")
                || !bidmsConfigProperties.getRest().get("provision").containsKey("new-uid")
                || bidmsConfigProperties.getRest().get("provision").get("new-uid").getUrl() == null) {
            throw new RuntimeException(BidmsConfigProperties.REST_KEY + ".provision.new-uid.url is not configured");
        }
        return bidmsConfigProperties.getRest().get("provision").get("new-uid").getUrl();
    }


    private String getProvisonRestUsername() {
        if (matchServiceConfigProperties.getRest() == null ||
                !matchServiceConfigProperties.getRest().containsKey("provision") ||
                matchServiceConfigProperties.getRest().get("provision").getUsername() == null) {
            throw new RuntimeException(MatchServiceConfigProperties.REST_KEY + ".provision.username is not configured");
        }
        return matchServiceConfigProperties.getRest().get("provision").getUsername();
    }

    private String getProvisionRestPassword() {
        if (matchServiceConfigProperties.getRest() == null ||
                !matchServiceConfigProperties.getRest().containsKey("provision") ||
                matchServiceConfigProperties.getRest().get("provision").getPassword() == null) {
            throw new RuntimeException(MatchServiceConfigProperties.REST_KEY + ".provision.password is not configured");
        }
        return matchServiceConfigProperties.getRest().get("provision").getPassword();
    }

    @Bean
    public ProvisionRestTemplate getProvisionRestTemplate(RestTemplateBuilder builder) {
        return RestClientUtil.configureSslDigestAuthRestTemplate(builder, getProvisionUidRestUrl(), getProvisonRestUsername(), getProvisionRestPassword(), new ProvisionRestTemplate());
    }

    @Bean
    public ConnectionFactory getJmsConnectionFactory() {
        if (bidmsConfigProperties.getJmsConnections() == null || !bidmsConfigProperties.getJmsConnections().containsKey("AMQ")) {
            throw new RuntimeException(BidmsConfigProperties.JMS_CONNECTIONS_KEY + ".AMQ is not configured");
        }
        JmsConnectionConfigProperties jmsConnectionConfig = bidmsConfigProperties.getJmsConnections().get("AMQ");
        if (jmsConnectionConfig.getBrokerUrl().startsWith("ssl")) {
            ActiveMQSslConnectionFactory amqConnectionFactory = new ActiveMQSslConnectionFactory();
            try {
                if (jmsConnectionConfig.getTrustStore() != null) {
                    amqConnectionFactory.setTrustStore(jmsConnectionConfig.getTrustStore());
                    amqConnectionFactory.setTrustStorePassword(jmsConnectionConfig.getTrustStorePassword());
                }
                if (jmsConnectionConfig.getKeyStore() != null) {
                    amqConnectionFactory.setKeyStore(jmsConnectionConfig.getKeyStore());
                    amqConnectionFactory.setKeyStorePassword(jmsConnectionConfig.getKeyStorePassword());
                }
            } catch (Exception e) {
                throw new RuntimeException("There was a problem configuring the JMS trust or key store", e);
            }
            amqConnectionFactory.setBrokerURL(jmsConnectionConfig.getBrokerUrl());
            amqConnectionFactory.setUserName(jmsConnectionConfig.getUsername());
            amqConnectionFactory.setPassword(jmsConnectionConfig.getPassword());
            return new PooledConnectionFactory(amqConnectionFactory);
        } else {
            ActiveMQConnectionFactory amqConnectionFactory = new ActiveMQConnectionFactory();
            amqConnectionFactory.setBrokerURL(jmsConnectionConfig.getBrokerUrl());
            amqConnectionFactory.setUserName(jmsConnectionConfig.getUsername());
            amqConnectionFactory.setPassword(jmsConnectionConfig.getPassword());
            return new PooledConnectionFactory(amqConnectionFactory);
        }
    }

    @Bean
    public ProvisionJmsTemplate getProvisionJmsTemplate(ConnectionFactory jmsConnectionFactory) {
        return new ProvisionJmsTemplate(jmsConnectionFactory);
    }
}
