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
package edu.berkeley.bidms.jms.util;

import edu.berkeley.bidms.app.common.config.properties.JmsConnectionConfigProperties;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.ConnectionFactory;

public class ConnectionFactoryUtil {

    private static RedeliveryPolicy createRedeliveryPolicy(JmsConnectionConfigProperties jmsConnectionConfig) {
        //
        // http://activemq.apache.org/redelivery-policy.html
        // http://activemq.apache.org/message-redelivery-and-dlq-handling.html
        //
        // It's important to set maximumRedeliveries to -1 here,
        // otherwise you risk having the message go into ActiveMQ's
        // DLQ (read link above) and once that happens, order of
        // message delivery can no longer be guaranteed.
        //
        // It also is advised that the ActiveMQ broker server be set
        // up to use the redeliveryPlugin (typically configured in
        // activemq.xml) to ensure anything that accidentally ends up
        // in the DLQ (due to a misconfigured client) will be
        // requeued.  This is explained at the bottom of
        // http://activemq.apache.org/message-redelivery-and-dlq-handling.html.
        // The problem with this is order is no longer guaranteed if
        // something is requeued from the DLQ (but that's better
        // than dropped messages).  But if all clients are properly
        // configured with maximumRedeliveries=-1, then nothing will
        // end up in the DLQ and this issue is averted.
        //
        RedeliveryPolicy rp = new RedeliveryPolicy();
        rp.setMaximumRedeliveries(jmsConnectionConfig.getMaximumRedeliveries() != null ? jmsConnectionConfig.getMaximumRedeliveries() : -1);
        rp.setInitialRedeliveryDelay(jmsConnectionConfig.getInitialRedeliveryDelay() != null ? jmsConnectionConfig.getInitialRedeliveryDelay() : 15000);
        rp.setRedeliveryDelay(jmsConnectionConfig.getRedeliveryDelay() != null ? jmsConnectionConfig.getRedeliveryDelay() : 15000);
        return rp;
    }

    public static ConnectionFactory buildConnectionFactory(JmsConnectionConfigProperties jmsConnectionConfig) {
        return (buildConnectionFactory(jmsConnectionConfig, (amqConnectionFactory) -> {
            amqConnectionFactory.setRedeliveryPolicy(createRedeliveryPolicy(jmsConnectionConfig));
        }));
    }

    public static ConnectionFactory buildConnectionFactory(JmsConnectionConfigProperties jmsConnectionConfig, ConnectionFactoryConfigurer configurer) {
        ActiveMQConnectionFactory amqConnectionFactory = null;
        if (jmsConnectionConfig.getBrokerUrl().startsWith("ssl")) {
            ActiveMQSslConnectionFactory amqSslConnectionFactory = new ActiveMQSslConnectionFactory();
            try {
                if (jmsConnectionConfig.getTrustStore() != null) {
                    amqSslConnectionFactory.setTrustStore(jmsConnectionConfig.getTrustStore());
                    amqSslConnectionFactory.setTrustStorePassword(jmsConnectionConfig.getTrustStorePassword());
                }
                if (jmsConnectionConfig.getKeyStore() != null) {
                    amqSslConnectionFactory.setKeyStore(jmsConnectionConfig.getKeyStore());
                    amqSslConnectionFactory.setKeyStore(jmsConnectionConfig.getKeyStore());
                    amqSslConnectionFactory.setKeyStorePassword(jmsConnectionConfig.getKeyStorePassword());
                }
            } catch (Exception e) {
                throw new RuntimeException("There was a problem configuring the JMS trust or key store", e);
            }
            amqConnectionFactory = amqSslConnectionFactory;
        } else {
            amqConnectionFactory = new ActiveMQConnectionFactory();
        }
        amqConnectionFactory.setBrokerURL(jmsConnectionConfig.getBrokerUrl());
        amqConnectionFactory.setUserName(jmsConnectionConfig.getUsername());
        amqConnectionFactory.setPassword(jmsConnectionConfig.getPassword());
        if (configurer != null) {
            configurer.configure(amqConnectionFactory);
        }
        return new PooledConnectionFactory(amqConnectionFactory);
    }
}
