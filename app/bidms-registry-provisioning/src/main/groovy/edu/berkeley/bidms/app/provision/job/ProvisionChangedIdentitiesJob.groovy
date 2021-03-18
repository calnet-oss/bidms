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
package edu.berkeley.bidms.app.provision.job

import edu.berkeley.bidms.app.common.config.properties.job.DailyCronJobConfigProperties
import edu.berkeley.bidms.app.provision.config.properties.ProvisioningConfigProperties
import edu.berkeley.bidms.app.provision.service.AbstractProvisionService
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
 * Queries PersonSorObjectsToProvisionView for identities that need to be
 * provisioned or reprovisioned because of new or changed SOR object data.
 *
 * Typically changes from the SOR Gateway Service are processed with
 * real-time messaging, so this Quartz job is a backup to that to catch
 * anything that fell through the cracks, or perhaps to pick up SORObject
 * changes that someone made manually.
 */
@Slf4j
@Component
@DisallowConcurrentExecution
@Configuration
class ProvisionChangedIdentitiesJob implements Job {
    @Slf4j
    static class JobTrigger extends CronTriggerImpl {
        JobTrigger(DailyCronJobConfigProperties config) {
            super()
            log.info("ProvisionChangedIdentitiesJob instantiating with config: $config")
            String runHour = config?.hour ?: "07"
            String runMin = config?.min ?: "46"
            log.info("ProvisionChangedIdentitiesJob instantiating with runHour=$runHour, runMin=$runMin and enabled=${config?.enabled}")
            setCronExpression("0 $runMin $runHour * * ?") // "s m h D M W Y"
            name = NAME
            description = DESCRIPTION
        }
    }

    static final String NAME = "provisionChangedIdentities"
    static final String DESCRIPTION = "Daily Provision Changed Identities Job"
    static final Class<Trigger> TRIGGER_CLASS = JobTrigger

    ProvisioningConfigProperties provisioningConfig
    AbstractProvisionService provisionService

    ProvisionChangedIdentitiesJob(ProvisioningConfigProperties provisioningConfig, AbstractProvisionService provisionService) {
        this.provisioningConfig = provisioningConfig
        this.provisionService = provisionService
    }

    DailyCronJobConfigProperties getConfig() {
        return provisioningConfig.job != null ? provisioningConfig.job.provisionChangedIdentities : null
    }

    @Override
    void execute(JobExecutionContext context) {
        if (!config?.enabled) {
            log.info("Quartz job is not enabled.  Not running: $DESCRIPTION")
            return
        }

        log.info("Running Quartz job: $DESCRIPTION")
        try {
            log.info("Bulk provisioning users from ToProvision view")
            try {
                provisionService.bulkProvision(false, AuditUtil.createEventId())
            }
            finally {
                log.info("Done with bulk provisioning of users")
            }
        }
        finally {
            log.info("Done running Quartz job: $DESCRIPTION")
        }
    }

    @Bean(name = "provisionChangedIdentitiesJobDetail")
    JobDetail getProvisionChangedIdentitiesJobDetail() {
        return JobBuilder.newJob().ofType(ProvisionChangedIdentitiesJob)
                .storeDurably()
                .withIdentity(ProvisionChangedIdentitiesJob.NAME)
                .withDescription(ProvisionChangedIdentitiesJob.DESCRIPTION)
                .build();
    }

    @Bean
    Trigger getProvisionChangedIdentitiesJobTrigger(@Qualifier("provisionChangedIdentitiesJobDetail") JobDetail jobDetail) {
        if (jobDetail.getKey().getName() == null) {
            throw new IllegalArgumentException("The given job has not yet had a name assigned to it.")
        }
        Trigger trigger = ProvisionChangedIdentitiesJob.TRIGGER_CLASS.getConstructor(DailyCronJobConfigProperties).newInstance([getConfig()] as Object[])
        trigger.jobKey = jobDetail.key
        return trigger
    }
}
