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

import edu.berkeley.bidms.app.matchservice.PersonExactMatch
import edu.berkeley.bidms.app.matchservice.PersonExistingMatch
import edu.berkeley.bidms.app.matchservice.PersonMatch
import edu.berkeley.bidms.app.matchservice.PersonNoMatch
import edu.berkeley.bidms.app.matchservice.PersonPartialMatch
import edu.berkeley.bidms.app.matchservice.PersonPartialMatches
import edu.berkeley.bidms.app.matchservice.rest.MatchEngineRestOperations
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.restclient.service.MatchEngineRestClientService
import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional(readOnly = true)
class MatchClientService {

    MatchEngineRestOperations restTemplate
    MatchEngineRestClientService matchEngineRestClientService
    PersonRepository personRepository

    MatchClientService(MatchEngineRestOperations restTemplate, MatchEngineRestClientService matchEngineRestClientService, PersonRepository personRepository) {
        this.restTemplate = restTemplate
        this.matchEngineRestClientService = matchEngineRestClientService
        this.personRepository = personRepository
    }

    /**
     * Call the match-engine to see if the database has a match on an existing record.
     * The match-engine configuration determins if there is no match, a single (canonical) match, or a partial match
     *
     * @param a map containing the some or all of the following properties (in this format)
     * [
     *      systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', fullName: 'lastName, firstName middleName', givenName: 'firstName', middleName: 'middleName', lastName: 'lastName',
     *      dateOfBirth: 'DOB', email: 'some@email.com', socialSecurityNumber: 'SSN', otherIds: [studentId: 'abc', employeeId: 'xyz'], matchOnly: false
     * ]
     * @return PersonMatch object
     * @throws RuntimeException a runtime exception if the match-engine returns other status codes than NOT_FOUND, OK, FOUND or MULTIPLE_CHOICES
     */
    PersonMatch match(String eventId, Map<String, Object> sorKeyData) {
        Map matchInputData = buildMatchInputData(sorKeyData)
        ResponseEntity<Map> response = matchEngineRestClientService.match(restTemplate, matchInputData)
        // The difference between OK and FOUND (I think) is that OK
        // indicates the SORObject matches up to an existing uid, where
        // as FOUND indicates the SORObject is already matched.  See
        // difference between the ExactMatchResponse (OK) and
        // ExistingMatchResponse (FOUND) in ucb-match.
        Map jsonResponse = response.body
        switch (response.statusCode) {
            case HttpStatus.NOT_FOUND:
                // matchOnly=true on input will cause person not to go to newUid queue
                return new PersonNoMatch(eventId: eventId, matchOnly: matchInputData.matchOnly as Boolean)
            case HttpStatus.OK:
                return exactMatch(eventId, jsonResponse)
            case HttpStatus.FOUND:
                return existingMatch(eventId, jsonResponse)
            case HttpStatus.MULTIPLE_CHOICES:
                return partialMatch(eventId, jsonResponse)
            default:
                log.error("Got wrong return code from match engine..")
                throw new RuntimeException("Got wrong return code from match engine: $response.statusCode.reasonPhrase ($response.statusCode) - ${response.body}")
        }

    }

    private PersonExactMatch exactMatch(String eventId, Map json) {
        // Person object is not to be changed
        Person person = personRepository.get(json.matchingRecord.referenceId as String)
        List<String> ruleNames = json.matchingRecord.ruleNames

        new PersonExactMatch(eventId: eventId, person: person, ruleNames: ruleNames)
    }

    private PersonExistingMatch existingMatch(String eventId, Map json) {
        def person = personRepository.get(json.matchingRecord.referenceId as String)
        new PersonExistingMatch(eventId: eventId, person: person)
    }

    private PersonPartialMatches partialMatch(String eventId, Map json) {
        def partialMatches = json.partialMatchingRecords.collect {
            // Person object is not to be changed
            Person person = personRepository.get(it.referenceId as String)
            List<String> ruleNames = it.ruleNames
            new PersonPartialMatch(eventId: eventId, person: person, ruleNames: ruleNames)
        }
        new PersonPartialMatches(eventId: eventId, partialMatches: partialMatches)
    }

    /**
     * Map input parameters to a Match-Engine request
     */
    private static Map<String, Object> buildMatchInputData(Map<String, Object> params) {
        Map<String, Object> map = [systemOfRecord: params.systemOfRecord, identifier: params.sorPrimaryKey]

        // Copy top level properties
        ['dateOfBirth', 'email', 'emailAddresses', 'phoneNumbers', 'matchOnly'].each {
            if (params[it]) {
                map[it] = params[it]
            }
        }

        // Copy name attributes to names structure
        def name = (['givenName', 'middleName', 'surName', 'fullName'].collectEntries {
            [it, params[it]]
        } as Map<String, Object>).findAll { it.value }

        if (name) {
            name.type = "official"
            map.names = [name]
        }

        if (params.socialSecurityNumber || params.otherIds) {
            // Copy other identifiers (comes in a map) to the identifiers list
            map.identifiers = params.otherIds?.collect { type, value ->
                [
                        type      : type,
                        identifier: value
                ]
            } ?: []

            // Finally add socialSecurityNumber if present
            if (params.socialSecurityNumber) {
                map.identifiers << [type: "socialSecurityNumber", identifier: params.socialSecurityNumber]
            }
        }

        return map
    }
}
