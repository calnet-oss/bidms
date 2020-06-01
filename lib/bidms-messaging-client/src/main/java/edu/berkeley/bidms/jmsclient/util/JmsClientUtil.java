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
package edu.berkeley.bidms.jmsclient.util;

import edu.berkeley.bidms.app.common.config.properties.JmsConnectionConfigProperties;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.ConnectionFactory;

public class JmsClientUtil {
    public static ConnectionFactory buildConnectionFactory(JmsConnectionConfigProperties jmsConnectionConfig) {
        if (jmsConnectionConfig.getBrokerUrl().startsWith("ssl")) {
            ActiveMQSslConnectionFactory amqConnectionFactory = new ActiveMQSslConnectionFactory();
            try {
                if (jmsConnectionConfig.getTrustStore() != null) {
                    amqConnectionFactory.setTrustStore(jmsConnectionConfig.getTrustStore());
                    amqConnectionFactory.setTrustStorePassword(jmsConnectionConfig.getTrustStorePassword());
                }
                if (jmsConnectionConfig.getKeyStore() != null) {
                    amqConnectionFactory.setKeyStore(jmsConnectionConfig.getKeyStore());
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
}
