/*
 * Copyright (c) 2014, Regents of the University of California and
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
package edu.berkeley.bidms.app.matchengine.service

import edu.berkeley.bidms.app.matchengine.config.MatchConfig
import edu.berkeley.bidms.app.matchengine.database.Candidate
import edu.berkeley.bidms.app.matchengine.response.ExactMatchResponse
import edu.berkeley.bidms.app.matchengine.response.ExistingMatchResponse
import edu.berkeley.bidms.app.matchengine.response.FuzzyMatchResponse
import edu.berkeley.bidms.app.matchengine.response.Response
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional
class PersonService {

    MatchService matchService
    MatchConfig matchConfig

    PersonService(MatchService matchService, MatchConfig matchConfig) {
        this.matchService = matchService
        this.matchConfig = matchConfig
    }

    /**
     * Will attempt to match a person
     * The input format could be like this:
     * [
     *      systemOfRecord: "ADVCON",
     *      identifier: '4000000',
     *      dateOfBirth: '1995-01-01',
     *      names: [
     *          [type: 'official', givenName: 'John', surName: 'Smith']
     *      ],
     *      identifiers: [
     *          [type: 'studentId', identifier: '60000000']
     *      ]
     * ]
     *
     * @param matchInput the format above
     * @return either an ExistingMatchResponse if the identifier is already in the registry
     *         A not found Response if no matches were found,
     *         an ExactMatchResponse if one and only one exact match was found
     *         or a FuzzyMatchResponse, if the match was not exact, but there were one or more fuzzymatches
     */
    Response matchPerson(Map matchInput) {
        def existingRecord = matchService.findExistingRecord(matchInput)
        if (existingRecord) {
            return new ExistingMatchResponse(responseData: existingRecord)
        }

        Set<Candidate> candidates = matchService.findCandidates(matchInput)
        if (!candidates) {
            log.debug("No match found for ${getRedactedParams(matchInput)}")
            return Response.NOT_FOUND
        } else if (candidates.size() == 1 && candidates[0].exactMatch) {
            return new ExactMatchResponse(responseData: candidates[0])
        } else if (sameReferenceIdAndCanonical(candidates)) {
            return new ExactMatchResponse(responseData: candidates[0])
        }
        // Fall through is always a Fuzzy Match.
        return new FuzzyMatchResponse(responseData: candidates)
    }

    boolean sameReferenceIdAndCanonical(Set<Candidate> candidates) {
        return candidates.referenceId.unique().size() == 1 && candidates.every { it.exactMatch }
    }

    // Redact SSN and DOB from log
    static Map getRedactedParams(Map params) {
        // Redact SSN and DOB from log
        def displayParams = [:]
        displayParams.putAll(params)
        if (displayParams.containsKey("socialSecurityNumber")) {
            displayParams.socialSecurityNumber = "*****"
        }
        if (displayParams.containsKey("dateOfBirth")) {
            displayParams.dateOfBirth = "****-**-**"
        }
        if (displayParams.containsKey("identifiers") && displayParams.identifiers instanceof List) {
            displayParams.identifiers.each { def idMap ->
                if (idMap.type == "socialSecurityNumber") {
                    idMap.identifier = "___-_*-****"
                }
            }
        }
        return displayParams
    }
}
