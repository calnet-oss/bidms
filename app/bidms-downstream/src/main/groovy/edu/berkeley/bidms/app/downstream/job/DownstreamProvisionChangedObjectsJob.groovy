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
package edu.berkeley.bidms.app.downstream.job

import edu.berkeley.bidms.app.downstream.config.properties.DownstreamConfigProperties
import edu.berkeley.bidms.app.downstream.config.properties.job.DownstreamProvisionChangedObjectsCronJobConfigProperties
import edu.berkeley.bidms.downstream.service.DownstreamProvisioningService
import edu.berkeley.bidms.downstream.service.ProvisioningResult
import edu.berkeley.bidms.logging.AuditUtil
import groovy.util.logging.Slf4j
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.Trigger
import org.quartz.impl.triggers.CronTriggerImpl
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

/**
 * Queries DownstreamObjectToProvisionView for objects that need to be
 * provisioned or updated in downstream systems.  This job doesn't directly
 * provision, but rather sends object IDs to the "to provision" queue to be
 * asynchronously processed off the queue.
 *
 * Typically changes from registry-provisioning are processed with real-time
 * messaging, so this Quartz job is a "backup" to clean up anything the
 * real-time processes missed or dropped.
 */
@Slf4j
@Component
@DisallowConcurrentExecution
@Configuration
class DownstreamProvisionChangedObjectsJob implements Job {
    static class JobTrigger extends CronTriggerImpl {
        JobTrigger(DownstreamProvisionChangedObjectsCronJobConfigProperties config) {
            super()
            log.debug("DownstreamProvisionChangedObjectsJob instantiating with config: $config")
            String runHour = config?.hour ?: "08"
            String runMin = config?.min ?: "46"
            boolean isEnabled = config?.enabled
            log.info("DownstreamProvisionChangedObjectsJob instantiating with runHour=$runHour, runMin=$runMin and enabled=$isEnabled")
            setCronExpression("0 $runMin $runHour * * ?") // "s m h D M W Y"
            name = NAME
            description = DESCRIPTION
        }
    }

    static final String NAME = "provisionChangedObjects"
    static final String DESCRIPTION = "Daily Provision Changed Objects Job"
    static final Class<Trigger> TRIGGER_CLASS = JobTrigger

    DownstreamConfigProperties downstreamConfigProperties
    // This is a Spring-injected map where the key is the
    // downstreamSystemName and the value is the provision service bean.
    Map<String, ? extends DownstreamProvisioningService> downstreamSystemServices

    DownstreamProvisionChangedObjectsJob(
            DownstreamConfigProperties downstreamConfigProperties,
            @Qualifier("downstreamSystemServices") Map<String, ? extends DownstreamProvisioningService> downstreamSystemServices
    ) {
        this.downstreamConfigProperties = downstreamConfigProperties
        this.downstreamSystemServices = downstreamSystemServices
        if (!downstreamSystemServices) {
            log.warn("The downstreamSystemServices map bean is ${downstreamSystemServices == null ? 'null' : 'empty'}")
        }
    }

    DownstreamProvisionChangedObjectsCronJobConfigProperties getConfig() {
        return downstreamConfigProperties.job?.provisionChangedObjects
    }

    @Override
    void execute(JobExecutionContext context) {
        if (!config?.enabled) {
            log.info("Quartz job is not enabled.  Not running: $DESCRIPTION")
            return
        }

        log.info("Running Quartz job: $DESCRIPTION")
        try {
            String eventId = AuditUtil.createEventId()
            config?.enabledSystems?.each { String systemName ->
                def provisionService = downstreamSystemServices[systemName]
                if (provisionService) {
                    ProvisioningResult provisioningResult = provisionService.provisionBulk(eventId, systemName)
                    log.info("${provisioningResult.count} objects sent to the downstream to-provision queue for $systemName")
                } else {
                    log.warn("Downstream system $systemName is enabled but there is no service configured for this system in the downstreamSystemServices map bean.  downstreamSystemServices map has the following keys: ${downstreamSystemServices?.keySet()}")
                }
            }
        }
        finally {
            log.info("Done running Quartz job: $DESCRIPTION")
        }
    }

    @Bean(name = "provisionChangedObjectsJobDetail")
    JobDetail getProvisionChangedObjectsJobDetail() {
        return JobBuilder.newJob().ofType(DownstreamProvisionChangedObjectsJob)
                .storeDurably()
                .withIdentity(NAME)
                .withDescription(DESCRIPTION)
                .build();
    }

    @Bean
    Trigger getProvisionChangedObjectsJobTrigger(@Qualifier("provisionChangedObjectsJobDetail") JobDetail jobDetail) {
        if (jobDetail.getKey().getName() == null) {
            throw new IllegalArgumentException("The given job has not yet had a name assigned to it.")
        }
        Trigger trigger = TRIGGER_CLASS.getConstructor(DownstreamProvisionChangedObjectsCronJobConfigProperties).newInstance([getConfig()] as Object[])
        trigger.jobKey = jobDetail.key
        return trigger
    }
}
