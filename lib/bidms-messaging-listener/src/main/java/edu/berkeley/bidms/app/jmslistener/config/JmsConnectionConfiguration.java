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
package edu.berkeley.bidms.app.jmslistener.config;

import edu.berkeley.bidms.app.common.config.properties.BidmsConfigProperties;
import edu.berkeley.bidms.jms.util.ConnectionFactoryUtil;
import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

@Configuration
public class JmsConnectionConfiguration {
    private BidmsConfigProperties bidmsConfigProperties;

    public JmsConnectionConfiguration(BidmsConfigProperties bidmsConfigProperties) {
        this.bidmsConfigProperties = bidmsConfigProperties;
    }

    @Bean(name = "amqJmsConnectionFactory")
    public ConnectionFactory getJmsConnectionFactory() {
        if (bidmsConfigProperties.getJmsConnections() == null || !bidmsConfigProperties.getJmsConnections().containsKey("AMQ")) {
            throw new RuntimeException(BidmsConfigProperties.JMS_CONNECTIONS_KEY + ".AMQ is not configured");
        }
        return ConnectionFactoryUtil.buildConnectionFactory(bidmsConfigProperties.getJmsConnections().get("AMQ"), false);
    }

    @Bean(name = "amqJmsListenerContainerFactory")
    public JmsListenerContainerFactory<?> getJmsListenerContainerFactory(
            @Qualifier("amqJmsConnectionFactory") ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer
    ) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setConcurrency("1-4");
        factory.setReceiveTimeout(30000L);
        return factory;
    }
}
