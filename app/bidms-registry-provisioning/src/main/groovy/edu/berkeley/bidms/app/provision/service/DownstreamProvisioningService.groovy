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
package edu.berkeley.bidms.app.provision.service

import edu.berkeley.bidms.app.jmsclient.service.DownstreamProvisioningJmsClientService
import edu.berkeley.bidms.app.provision.config.properties.ProvisioningConfigProperties
import edu.berkeley.bidms.app.registryModel.model.type.DownstreamSystemEnum
import edu.berkeley.bidms.app.restclient.service.DownstreamProvisionRestClientService
import edu.berkeley.bidms.provision.jms.DownstreamProvisionJmsTemplate
import edu.berkeley.bidms.provision.rest.DownstreamProvisioningRestOperations
import groovy.util.logging.Slf4j
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Service

@Slf4j
// If you wish to override this bean, create your own with @Service("downstreamProvisioningService")
@ConditionalOnMissingBean(name = "downstreamProvisioningService")
@Service("edu.berkeley.bidms.app.provision.service.DownstreamProvisioningService")
class DownstreamProvisioningService {
    ProvisioningConfigProperties provisionConfig
    DownstreamProvisioningRestOperations downstreamProvisionRestTemplate
    DownstreamProvisionRestClientService downstreamRestClientService
    DownstreamProvisionJmsTemplate downstreamProvisionJmsTemplate
    DownstreamProvisioningJmsClientService downstreamProvisioningJmsClientService

    DownstreamProvisioningService(
            ProvisioningConfigProperties provisionConfig,
            DownstreamProvisioningRestOperations downstreamProvisionRestTemplate,
            DownstreamProvisionRestClientService downstreamRestClientService,
            DownstreamProvisionJmsTemplate downstreamProvisionJmsTemplate,
            DownstreamProvisioningJmsClientService downstreamProvisioningJmsClientService
    ) {
        this.provisionConfig = provisionConfig
        this.downstreamProvisionRestTemplate = downstreamProvisionRestTemplate
        this.downstreamRestClientService = downstreamRestClientService
        this.downstreamProvisionJmsTemplate = downstreamProvisionJmsTemplate
        this.downstreamProvisioningJmsClientService = downstreamProvisioningJmsClientService
    }

    void provisionUidAsynchronously(String eventId, String uid) {
        final Map<String, ?> headers = [
                eventId   : eventId,
                fromSource: "reg-prov"
        ]
        def downstreamSystems = [DownstreamSystemEnum.LDAP.name]
        if (adEnabled) {
            downstreamSystems << DownstreamSystemEnum.AD.name
        }
        downstreamSystems.each { String downstreamSystemName ->
            downstreamProvisioningJmsClientService.provisionUid(downstreamProvisionJmsTemplate, downstreamSystemName, uid, headers)
        }
    }

    void provisionUidSynchronously(String eventId, String uid) {
        Map<String, Object> mainEntryResponse = provisionMainEntry(eventId, uid)
        Map<String, Object> adEntryResponse = (adEnabled ? provisionAdEntry(eventId, uid) : null)

        checkResponse(mainEntryResponse, uid, DownstreamSystemEnum.LDAP.name, false)
        if (adEnabled) {
            checkResponse(adEntryResponse, uid, DownstreamSystemEnum.AD.name, false)
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    protected void checkResponse(Map<String, Object> response, String uid, String downstreamSystemName, boolean checkGreaterThanZeroPersistCount) {
        if ((response.containsKey("persistCount") && (checkGreaterThanZeroPersistCount ? response.persistCount == 1 : response.persistCount != null)) ||
                (response.containsKey("singleObjectUnchanged") && response.singleObjectUnchanged)) {
            log.debug("Successful synchronous reprovision to $downstreamSystemName of uid $uid via REST")
        } else {
            throw new RuntimeException("When synchronously reprovisioning uid $uid to $downstreamSystemName, got an OK response from REST endpoint, but the response payload did not confirm the uid was successfully provisioned.  REST response: $response")
        }
    }

    protected boolean isAdEnabled() {
        return provisionConfig.provisioningContext?.ad?.enabled
    }

    private Map<String, Object> provisionMainEntry(String eventId, String uid) {
        Map<String, Object> response = downstreamRestClientService.provisionUidCheckResponse(downstreamProvisionRestTemplate, DownstreamSystemEnum.LDAP.name, uid, eventId);
        log.debug("Got response for main entry: $response")
        return response
    }

    private Map<String, Object> provisionAdEntry(String eventId, String uid) {
        Map<String, Object> response = downstreamRestClientService.provisionUidCheckResponse(downstreamProvisionRestTemplate, DownstreamSystemEnum.AD.name, uid, eventId);
        log.debug("Got response for AD entry: $response")
        return response
    }
}
