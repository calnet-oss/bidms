/*
 * Copyright (c) 2015, Regents of the University of California and
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
package edu.berkeley.bidms.app.matchservice.service

import edu.berkeley.bidms.app.matchservice.config.properties.JmsEndpointConfigProperties
import edu.berkeley.bidms.app.matchservice.config.properties.MatchServiceConfigProperties
import edu.berkeley.bidms.app.matchservice.jms.ProvisionJmsTemplate
import edu.berkeley.bidms.app.registryModel.model.Person
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DownstreamJMSService {

    MatchServiceConfigProperties matchServiceConfigProperties
    ProvisionJmsTemplate jmsTemplate

    DownstreamJMSService(MatchServiceConfigProperties matchServiceConfigProperties, ProvisionJmsTemplate jmsTemplate) {
        this.matchServiceConfigProperties = matchServiceConfigProperties;
        this.jmsTemplate = jmsTemplate
    }

    /**
     * Notify downstream systems (The registry) that a Person is ready to (re)provision
     */
    def provision(Person person) {
        if (matchServiceConfigProperties.getJms() == null || !matchServiceConfigProperties.getJms().containsKey("provision-uid")) {
            throw new RuntimeException(MatchServiceConfigProperties.JMS_KEY + ".provision-uid is not configured");
        }
        JmsEndpointConfigProperties jmsEndpointConfigProperties = matchServiceConfigProperties.getJms().get("provision-uid")
        if (!jmsEndpointConfigProperties.getQueueName()) {
            throw new RuntimeException(MatchServiceConfigProperties.JMS_KEY + ".provision-uid.queue-name is not configured")
        }
        jmsTemplate.convertAndSend(jmsEndpointConfigProperties.getQueueName(), [uid: person.uid])
    }
}
