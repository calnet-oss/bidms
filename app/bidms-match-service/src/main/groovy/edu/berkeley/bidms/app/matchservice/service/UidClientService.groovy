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

import edu.berkeley.bidms.app.matchservice.rest.ProvisionRestOperations
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.PersonSorObjectsJson
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.repo.PersonSorObjectsJsonRepository
import edu.berkeley.bidms.app.restclient.service.ProvisionRestClientService
import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional(rollbackFor = Exception)
class UidClientService {

    ProvisionRestOperations restTemplate
    ProvisionRestClientService provisionRestClientService
    PersonSorObjectsJsonRepository personSorObjectsJsonRepository

    UidClientService(ProvisionRestOperations restTemplate, ProvisionRestClientService provisionRestClientService, PersonSorObjectsJsonRepository personSorObjectsJsonRepository) {
        this.restTemplate = restTemplate
        this.provisionRestClientService = provisionRestClientService
        this.personSorObjectsJsonRepository = personSorObjectsJsonRepository
    }

    /**
     * Makes a REST call to the Registry Provisioning to provision the given person
     *
     * @param Person to provision
     */
    void provisionUid(Person person, boolean synchronousDownstream = true) {
        // synchronousDownstream=true means synchronous downstream directory provisioning
        ResponseEntity<Map> response = provisionRestClientService.provisionUid(restTemplate, person.uid, synchronousDownstream)
        if (response.statusCode != HttpStatus.OK) {
            log.error("Error provisioning existing person ${person.uid}, response code: ${response.statusCode}:${response.body}")
            // Since the reprovision REST call failed, we need to mark the forceProvision flag directly in the database so the uid is reprovisioned later.
            try {
                markForReprovision(person.uid)
            }
            catch (Exception e) {
                log.error("Couldn't set the forceProvision flag to true for uid ${person.uid} because an exception occurred: ${e.message}")
            }
        } else {
            log.debug "Successfully provisioned exising person ${person.uid}"
        }
    }

    /**
     * Makes a REST call to the Registry Provisioning to assign new UID to person and provision
     *
     * @param sorObject the SORObject to pass to Registry Provisioning
     */
    String provisionNewUid(SORObject sorObject, boolean synchronousDownstream = true) {
        // synchronousDownstream=true means synchronous downstream directory provisioning
        if (sorObject?.id == null) {
            throw new IllegalArgumentException("sorObject.id cannot be null")
        }
        ResponseEntity<Map> response = provisionRestClientService.provisionNewUid(restTemplate, sorObject.id, synchronousDownstream)
        if (response.statusCode != HttpStatus.OK) {
            log.error("Could not generate a new uid for sorObject ${sorObject.id}, response code: ${response.statusCode}:${response.body}")
            return null
        } else if (!response.body?.provisioningSuccessful) {
            if (response.body?.provisioningErrorMessage) {
                String uid = response.body.uid
                log.warn "Error provisioning new sorObject $sorObject.id for person $uid: ${response.body.provisioningErrorMessage}"
                if (uid) {
                    // Since the reprovision REST call failed, we need to mark the forceProvision flag directly in the database so the uid is reprovisioned later.
                    try {
                        markForReprovision(uid)
                    }
                    catch (Exception e) {
                        log.error("Couldn't set the forceProvision flag to true for uid $uid because an exception occurred: ${e.message}")

                    }
                }
            } else {
                log.warn "Error provisioning new sorObject $sorObject.id: ${response.body}"
            }
            return null
        } else {
            log.debug "Successfully provisioned new sorObject sorObjectId=$response.body.sorObjectId, sorPrimaryKey=${sorObject.sorPrimaryKey}, sorName=${sorObject.sor.name} for person with new uid ${response.body.uid}"
            return response.body.uid
        }
    }

    private static enum MarkForReprovisionResult {
        MARKED, MISSING, ALREADY_MARKED
    }

    private MarkForReprovisionResult markForReprovision(String uid) {
        PersonSorObjectsJson personSorObjectsJson = personSorObjectsJsonRepository.get(uid);
        if (!personSorObjectsJson) {
            log.error("Couldn't set the forceProvision flag to true for uid $uid because there is no PersonSorObjectsJson row for this uid.")
            return MarkForReprovisionResult.MISSING
        }
        if (!personSorObjectsJson.forceProvision) {
            personSorObjectsJson.forceProvision = true;
            personSorObjectsJsonRepository.saveAndFlush(personSorObjectsJson)
            return MarkForReprovisionResult.MARKED
        } else {
            return MarkForReprovisionResult.ALREADY_MARKED
        }
    }
}
