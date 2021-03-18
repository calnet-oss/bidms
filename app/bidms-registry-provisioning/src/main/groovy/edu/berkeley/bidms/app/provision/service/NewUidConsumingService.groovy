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
package edu.berkeley.bidms.app.provision.service

import edu.berkeley.bidms.logging.AuditSuccessEvent
import edu.berkeley.bidms.logging.AuditUtil
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import javax.jms.MapMessage
import javax.jms.Message

@Slf4j
@Service
class NewUidConsumingService {

    static enum AuditOperation {
        newUidAsynchronous
    }

    @Value('${bidms.provision.app-name}')
    private String APP_NAME

    NewUidService newUidService

    NewUidConsumingService(NewUidService newUidService) {
        this.newUidService = newUidService
    }

    @Transactional(propagation = Propagation.NEVER)
    @JmsListener(destination = '${bidms.jms.provision.new-uid.queue-name}', containerFactory = '${bidms.provision.jms.provision.jms-listener-container-factory-bean-name}')
    void receiveMessage(Message msg) {
        if (!(msg instanceof MapMessage)) {
            throw new RuntimeException("JMS Message is not of expected MapMessage type")
        }
        processMessage((MapMessage) msg)
    }

    // mandatory: sorObjectId
    // optional: synchronousDownstream
    void processMessage(MapMessage message) {
        try {
            String eventId = AuditUtil.createEventId()
            long sorObjectId = message.getLong("sorObjectId")
            Boolean synchronousDownstream = message.getBoolean("synchronousDownstream")
            NewUidService.NewUidResult result = newUidService.provisionNewUid(sorObjectId, synchronousDownstream, eventId)
            if (!result.uidGenerationSuccessful) {
                throw new RuntimeException("UID generation was not successful for sorObjectId $sorObjectId")
            } else if (result.hasExistingUid) {
                log.warn("sorObjectId=$sorObjectId, sorPrimaryKey=${result.sorPrimaryKey}, sorName=${result.sorName} was successfully consumed from the newUid queue but that SORObject already had a uid (${result.uid}) assigned to it.  Not reassigning a new uid.")
            } else {
                log.info("sorObjectId=$sorObjectId, sorPrimaryKey=${result.sorPrimaryKey}, sorName=${result.sorName} was successfully assigned a new uid: ${result.uid}")
            }
            AuditUtil.logAuditEvent(APP_NAME, new AuditSuccessEvent(eventId: eventId,
                    op: AuditOperation.newUidAsynchronous,
                    forUid: result.uid,
                    attrs: [
                            sorObjectId           : sorObjectId,
                            sorPrimaryKey         : result.sorPrimaryKey,
                            sorName               : result.sorName,
                            provisioningSuccessful: result.provisioningSuccessful,
                            hasExistingUid        : result.hasExistingUid
                    ]))
            if (!result.provisioningSuccessful) {
                log.warn("sorObjectId=$sorObjectId has uid ${result.uid} assigned, but there was a problem reprovisioning it: ${result.provisioningException}")
            }
        }
        catch (Exception e) {
            log.error("processMessage failed", e)
            throw e
        }
    }
}
