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
package edu.berkeley.bidms.app.provision.service

import edu.berkeley.bidms.logging.AuditUtil
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import jakarta.jms.MapMessage
import jakarta.jms.Message

@Slf4j
@Service
class ProvisionUidConsumingService {
    ProvisionService provisionService

    private static final Object lock = new Object()
    private static volatile long counter = 0
    private static volatile long totalProcessingTime = 0
    private static volatile long startBatchTime = 0
    private static volatile long batchCount = 0
    private static volatile long totalBatchTimeSeconds = 0
    private static final int batchSize = 1000

    ProvisionUidConsumingService(ProvisionService provisionService) {
        this.provisionService = provisionService
    }

    @Transactional(propagation = Propagation.NEVER)
    @JmsListener(destination = '${bidms.jms.provision.provision-uid.queue-name}', containerFactory = '${bidms.provision.jms.provision.jms-listener-container-factory-bean-name}')
    void receiveProvisionUidMessage(Message msg) {
        handleMessage(msg)
    }

    @Transactional(propagation = Propagation.NEVER)
    @JmsListener(destination = '${bidms.jms.provision.provision-uid-bulk.queue-name}', containerFactory = '${bidms.provision.jms.provision.jms-listener-container-factory-bean-name}')
    void receiveProvisionUidBulkMessage(Message msg) {
        handleMessage(msg)
    }

    void handleMessage(Message msg) {
        try {
            if (!(msg instanceof MapMessage)) {
                throw new RuntimeException("JMS Message is not of expected MapMessage type")
            }
            MapMessage message = (MapMessage) msg

            String uid = message.getString("uid")
            log.debug("provisioning $uid")

            if (!uid) {
                // Not making this fatal in case an app inadvertantly places
                // unmatched people into the queue.  The messages will be
                // consumed off the queue, but nothing will be done with
                // them other than logging this warning.
                log.warn("uid couldn't be obtained from message: mapNames=${message.mapNames.collect { it }}, jmsMessageID=${message.getJMSMessageID()}")
                return
            }

            long start = new Date().time
            provisionService.provisionUid(uid, false, AuditUtil.createEventId())

            synchronized (lock) {
                totalProcessingTime += new Date().time - start
                if (counter > 0 && (counter % batchSize == 0)) {
                    long endBatchTime = totalProcessingTime
                    batchCount++
                    long batchTimeSeconds = Math.round((endBatchTime - startBatchTime) / 1000)
                    totalBatchTimeSeconds += batchTimeSeconds
                    log.info("batch $batchCount: batchSize=" + batchSize +
                            ", batchTime=" + batchTimeSeconds +
                            "s, avgBatchTime=" + Math.round(totalBatchTimeSeconds / batchCount) + "s" +
                            ", " + getMemoryUsage())
                    startBatchTime = totalProcessingTime
                }
                counter++
            }

            log.debug("provisioning of $uid successful")
        }
        catch (Exception e) {
            log.error("provisionUid consumer threw an exception", e)
            // Not making this fatal so we don't block consuming of other
            // messages off the queue.  Instead, we are going to rely on the
            // Quartz provisioning job to later retry any provisioning
            // failures.
            return // return instead of rethrow so msg is consumed
        }
    }

    private static final long roundMB(long val) {
        return Math.round(val / 1000000);
    }

    static String getMemoryUsage() {
        Runtime rt = Runtime.getRuntime();
        long usedMem = rt.totalMemory() - rt.freeMemory();
        return ("JVM Memory: used=" + roundMB(usedMem)
                + "M, total=" + roundMB(rt.totalMemory())
                + "M, max=" + roundMB(rt.maxMemory())
                + "M, free=" + roundMB(rt.freeMemory()) + "M");
    }
}
