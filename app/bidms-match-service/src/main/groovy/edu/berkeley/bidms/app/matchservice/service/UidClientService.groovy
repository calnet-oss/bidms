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

import edu.berkeley.bidms.app.matchservice.config.MatchServiceConfiguration
import edu.berkeley.bidms.app.matchservice.rest.ProvisionRestOperations
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.SORObject
import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional(rollbackFor = Exception)
class UidClientService {

    MatchServiceConfiguration matchServiceConfiguration
    ProvisionRestOperations restTemplate

    UidClientService(MatchServiceConfiguration matchServiceConfiguration, ProvisionRestOperations restTemplate) {
        this.matchServiceConfiguration = matchServiceConfiguration
        this.restTemplate = restTemplate
    }

    /**
     * Makes a REST call to the Registry Provisioning to provision the given person
     * @param Person to provision
     * @throws RuntimeException if response status is not {@link HttpStatus#OK}
     */
    void provisionUid(Person person, boolean synchronousDownstream = true) {
        // synchronousDownstream=true means synchronous downstream directory provisioning
        String url = "${matchServiceConfiguration.restProvisionUidUrl.toString()}?uid=${person.uid}" + (synchronousDownstream ? "&synchronousDownstream=true" : "")
        ResponseEntity<Map> response = restTemplate.exchange(
                RequestEntity
                        .post(new URI(url))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(""),
                Map
        )
        if (response.statusCode != HttpStatus.OK) {
            log.error("Error provisioning existing person ${person.uid}, response code: ${response.statusCode}:${response.body}")
        }
        log.debug "Successfully provisioned exising person ${person.uid}"
    }

    /**
     * Makes a REST call to the Registry Provisioning to assign new UID to person and provision
     * @param sorObject the SORObject to pass to Registry Provisioning
     * @throws RuntimeException if response status is not {@link HttpStatus#OK}
     */
    String provisionNewUid(SORObject sorObject, boolean synchronousDownstream = true) {
        // synchronousDownstream=true means synchronous downstream directory provisioning
        String url = "${matchServiceConfiguration.restProvisionNewUidUrl.toString()}?sorObjectId=${sorObject.id}" + (synchronousDownstream ? "&synchronousDownstream=true" : "")
        ResponseEntity<Map> response = restTemplate.exchange(
                RequestEntity
                        .post(new URI(url))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(""),
                Map
        )
        if (response.statusCode != HttpStatus.OK) {
            log.error("Could not generate a new uid for sorObject ${sorObject.id}, response code: ${response.statusCode}:${response.text}")
            return null
        } else if (!response.body?.provisioningSuccessful) {
            if (response.body?.provisioningErrorMessage) {
                log.warn "Error provisioning new sorObject $sorObject.id for person ${response.body.uid}: ${response.body.provisioningErrorMessage}"
            } else {
                log.warn "Error provisioning new sorObject $sorObject.id: ${response.body}"
            }
            return null
        } else {
            log.debug "Successfully provisioned new sorObject sorObjectId=$response.body.sorObjectId, sorPrimaryKey=${sorObject.sorPrimaryKey}, sorName=${sorObject.sor.name} for person with new uid ${response.body.uid}"
            return response.body.uid
        }
    }
}
