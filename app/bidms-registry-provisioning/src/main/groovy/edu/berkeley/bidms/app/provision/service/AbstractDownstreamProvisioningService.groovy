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

import edu.berkeley.bidms.app.provision.config.properties.ProvisioningConfigProperties
import groovy.util.logging.Slf4j

@Slf4j
abstract class AbstractDownstreamProvisioningService {
    ProvisioningConfigProperties provisionConfig

    AbstractDownstreamProvisioningService(ProvisioningConfigProperties provisionConfig) {
        this.provisionConfig = provisionConfig
    }

    abstract void provisionUidAsynchronously(String eventId, String uid);

    abstract void provisionUidSynchronously(String eventId, String uid);

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
}
