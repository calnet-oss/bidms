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

import edu.berkeley.bidms.downstream.service.DownstreamProvisioningService
import edu.berkeley.bidms.downstream.service.DownstreamSystemNotFoundException
import edu.berkeley.bidms.downstream.service.ProvisioningResult
import groovy.util.logging.Slf4j
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Service

@Slf4j
// If you wish to override this bean, create your own with @Service("downstreamProvisionService")
@ConditionalOnMissingBean(name = "downstreamProvisionService")
@Service("edu.berkeley.bidms.app.downstream.service.DownstreamProvisionService")
class DownstreamProvisionService {
    Map<String, DownstreamProvisioningService> registrationMap = [:]

    void register(DownstreamProvisioningService provisioningService) {
        provisioningService.accepts()?.each { String downstreamSystemName ->
            registrationMap.put(downstreamSystemName, provisioningService)
            log.info("Registered ${provisioningService.getClass().name} to handle provisioning requests for downstreamSystem ${downstreamSystemName}")
        }
    }

    /**
     * @return A Map containing the operation result.
     */
    ProvisioningResult provision(String eventId, String downstreamSystemName, String uid = null, boolean forceAsynchronous = false, boolean skipIfUnchanged = false) {
        DownstreamProvisioningService downstreamProvisioningService = registrationMap.get(downstreamSystemName?.toUpperCase())
        if (downstreamProvisioningService) {
            if (!uid) {
                return downstreamProvisioningService.provisionBulk(eventId, downstreamSystemName, !forceAsynchronous)
            } else {
                return downstreamProvisioningService.provisionUid(eventId, downstreamSystemName, uid, forceAsynchronous, skipIfUnchanged)
            }
        } else {
            throw new DownstreamSystemNotFoundException(downstreamSystemName?.toUpperCase())
        }
    }
}
