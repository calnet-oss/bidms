/*
 * Copyright (c) 2017, Regents of the University of California and
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
package edu.berkeley.bidms.app.downstream.service

import edu.berkeley.bidms.downstream.service.ProvisioningResult
import edu.berkeley.bidms.logging.AuditUtil
import groovy.transform.Synchronized
import groovy.util.logging.Slf4j
import jakarta.jms.MapMessage
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service

@Service
@Slf4j
class UidQueueConsumerService {

    DownstreamProvisionService provisionService

    UidQueueConsumerService(DownstreamProvisionService provisionService) {
        this.provisionService = provisionService
    }

    // fields for tracking timing statistics
    private static final Map<String, Long> counterMap = [:]
    private static final Map<String, Long> totalProcessingMillisecondsMap = [:]
    private static final Map<String, Long> startBatchMillisecondsMap = [:]
    private static final int batchSize = 1000

    @JmsListener(destination = '${bidms.jms.downstream.provision-uid.queue-name}', containerFactory = '${bidms.downstream.jms.downstream.jms-listener-container-factory-bean-name}')
    void consume(MapMessage message) {
        try {
            String eventId = message.getStringProperty("eventId") ?: AuditUtil.createEventId()

            String downstreamSystemName = message.getString("downstreamSystemName")
            if (!downstreamSystemName) {
                throw new RuntimeException("downstreamSystemName is missing in the queue message")
            }

            String uid = message.getString("uid")
            if (!uid) {
                throw new RuntimeException("uid is missing in the queue message")
            }

            long start = new Date().time
            // We're already consuming asynchronously off ActiveMQ with parallel
            // consumers, so that's why we call provision() synchronously here.
            ProvisioningResult result = provisionService.provision(eventId, downstreamSystemName, uid, false, true)
            checkTimingStats(downstreamSystemName.toLowerCase(), start, result)
        }
        catch (Exception e) {
            log.error("There was an error trying to provision uid " + message.getString("uid") + " downstream to " + message.getString("downstreamSystemName"), e);
        }
    }

    @Synchronized
    private final void checkTimingStats(String tagPrefix, long start, ProvisioningResult result) {
        if (!counterMap.containsKey("${tagPrefix}Changed")) {
            counterMap["${tagPrefix}Changed"] = 0
            counterMap["${tagPrefix}Unchanged"] = 0
            totalProcessingMillisecondsMap["${tagPrefix}Changed"] = 0
            totalProcessingMillisecondsMap["${tagPrefix}Unchanged"] = 0
            startBatchMillisecondsMap["${tagPrefix}Changed"] = 0
            startBatchMillisecondsMap["${tagPrefix}Unchanged"] = 0
        }

        long end = new Date().time
        boolean changed = result.count
        boolean notChanged = result.unchangedCount
        if (changed) {
            totalProcessingMillisecondsMap["${tagPrefix}Changed"] += end - start
        } else if (notChanged) {
            totalProcessingMillisecondsMap["${tagPrefix}Unchanged"] += end - start
        }

        if (changed && counterMap["${tagPrefix}Changed"] > 0 && (counterMap["${tagPrefix}Changed"] % batchSize == 0)) {
            reportBatchTime("${tagPrefix}Changed")
        } else if (notChanged && counterMap["${tagPrefix}Unchanged"] > 0 && (counterMap["${tagPrefix}Unchanged"] % batchSize == 0)) {
            reportBatchTime("${tagPrefix}Unchanged")
        }

        if (changed) {
            counterMap["${tagPrefix}Changed"]++
        } else if (notChanged) {
            counterMap["${tagPrefix}Unchanged"]++
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private final void reportBatchTime(String tag) {
        int batch = counterMap[tag] / batchSize
        long batchTimeMilliseconds = totalProcessingMillisecondsMap[tag] - startBatchMillisecondsMap[tag]
        long avgSecondsPerBatch = Math.round((totalProcessingMillisecondsMap[tag] / 1000) / batch)
        long avgEntriesPerMin = Math.round(counterMap[tag] / (totalProcessingMillisecondsMap[tag] / 1000 / 60))
        log.info("From AMQ: $tag batch ${batch}: batchSize=$batchSize" +
                ", ${tag}BatchTime=${batchTimeMilliseconds / 1000}s" +
                ", ${tag}AvgSecondsPerBatch=${avgSecondsPerBatch}s" +
                ", ${tag}AvgEntriesPerMin=${avgEntriesPerMin}")
        startBatchMillisecondsMap[tag] = totalProcessingMillisecondsMap[tag]
    }
}
