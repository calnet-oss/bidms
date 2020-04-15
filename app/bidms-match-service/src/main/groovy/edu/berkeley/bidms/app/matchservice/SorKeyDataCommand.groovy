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
package edu.berkeley.bidms.app.matchservice

import edu.berkeley.bidms.app.matchservice.service.NewSORConsumerService
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import groovy.util.logging.Slf4j

import javax.validation.constraints.NotNull

@Slf4j
class SorKeyDataCommand {
    SORRepository sorRepository
    SORObjectRepository sorObjectRepository

    // these correspond to properties in SorKeyData from the
    // registry-sor-key-data plugin

    @NotNull
    String systemOfRecord

    @NotNull
    String sorPrimaryKey

    String uid
    String givenName
    String middleName
    String surName
    String fullName
    String email
    String dateOfBirth
    String socialSecurityNumber
    Map otherIds = [:]
    Boolean matchOnly
    boolean synchronousDownstream = true

    @NotNull
    SORObject getSorObject() {
        log.debug("Loading SORObject for $systemOfRecord/$sorPrimaryKey")
        def sorObject = sorObjectRepository.findBySorAndSorPrimaryKey(sorRepository.findByName(systemOfRecord), sorPrimaryKey)
        log.debug("-- found: $sorObject")
        return sorObject
    }

    Map<String, Object> getAttributes() {
        def sorAttributes = NewSORConsumerService.MATCH_STRING_FIELDS.findAll { this[it] }.collectEntries { [it, this[it].toString()] } +
                NewSORConsumerService.MATCH_BOOLEAN_FIELDS.findAll { this[it] }.collectEntries { [it, this[it] as Boolean] }
        if (otherIds) {
            sorAttributes.otherIds = otherIds
        }
        sorAttributes
    }
}
