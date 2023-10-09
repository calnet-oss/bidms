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
package edu.berkeley.bidms.app.matchservice.controller

import edu.berkeley.bidms.app.common.service.ValidationService
import edu.berkeley.bidms.app.matchservice.SorKeyDataCommand
import edu.berkeley.bidms.app.matchservice.service.NewSORConsumerService
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import edu.berkeley.bidms.app.restservice.common.response.BadRequestException
import edu.berkeley.bidms.common.validation.ValidationException
import edu.berkeley.bidms.logging.AuditUtil
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Slf4j
@RequestMapping(value = "/match-service")
@RestController
class TriggerMatchController {
    NewSORConsumerService newSORConsumerService
    ValidationService validationService
    SORRepository sorRepository
    SORObjectRepository sorObjectRepository

    TriggerMatchController(NewSORConsumerService newSORConsumerService, ValidationService validationService, SORRepository sorRepository, SORObjectRepository sorObjectRepository) {
        this.newSORConsumerService = newSORConsumerService
        this.validationService = validationService
        this.sorRepository = sorRepository
        this.sorObjectRepository = sorObjectRepository
    }

    @PackageScope
    void setNewSORConsumerService(NewSORConsumerService newSORConsumerService) {
        this.newSORConsumerService = newSORConsumerService
    }

    @PostMapping(value = "/internal/api/trigger-match", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, String> matchPerson(@RequestBody SorKeyDataCommand sorKeyDataCommand) {
        String eventId = AuditUtil.createEventId()
        sorKeyDataCommand.sorRepository = sorRepository
        sorKeyDataCommand.sorObjectRepository = sorObjectRepository
        try {
            validationService.validate(sorKeyDataCommand)
        }
        catch (ValidationException e) {
            log.error("could not trigger a match: $e.errors")
            throw new BadRequestException("could not trigger a match", e)
        }

        log.debug("Sor Key Data attributes. $sorKeyDataCommand.attributes")
        Map<String, String> result = newSORConsumerService.matchPerson(eventId, sorKeyDataCommand.sorObject, sorKeyDataCommand.attributes, sorKeyDataCommand.synchronousDownstream)
        return result ?: [:]
    }
}
