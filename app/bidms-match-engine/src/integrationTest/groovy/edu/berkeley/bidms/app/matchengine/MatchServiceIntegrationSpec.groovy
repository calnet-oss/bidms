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
package edu.berkeley.bidms.app.matchengine

import edu.berkeley.bidms.app.matchengine.database.Candidate
import edu.berkeley.bidms.app.matchengine.service.DatabaseService
import edu.berkeley.bidms.app.matchengine.service.MatchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class MatchServiceIntegrationSpec extends Specification {
    @Autowired
    MatchService service

    def setup() {
        service.databaseService = Mock(DatabaseService)
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    void "test findCandidates where superCanonical returns a match"() {
        when:
        service.findCandidates([systemOfRecord: "sis", identifier: "123", a: "b"])

        then:
        1 * service.databaseService.searchDatabase([systemOfRecord: "sis", identifier: "123", a: "b"], ConfidenceType.SUPERCANONICAL) >> [new Candidate(referenceId: "ref123", exactMatch: true)]

        and: "There are no other calls to the service"
        0 * service.databaseService._(*_)
        0 * service._(*_)
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    void "test findCandidates where canonical returns a match"() {
        when:
        service.findCandidates([systemOfRecord: "sis", identifier: "123", a: "b"])

        then:
        1 * service.databaseService.searchDatabase([systemOfRecord: "sis", identifier: "123", a: "b"], ConfidenceType.SUPERCANONICAL) >> []
        1 * service.databaseService.searchDatabase([systemOfRecord: "sis", identifier: "123", a: "b"], ConfidenceType.CANONICAL) >> [new Candidate()]

        and: "There are no other calls to the service"
        0 * service.databaseService._(*_)
        0 * service._(*_)
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    void "test findCandidates where superCanonical and canonical do not return a match"() {
        when:
        service.findCandidates([systemOfRecord: "sis", identifier: "123", a: "b"])

        then:
        1 * service.databaseService.searchDatabase([systemOfRecord: "sis", identifier: "123", a: "b"], ConfidenceType.SUPERCANONICAL) >> []
        1 * service.databaseService.searchDatabase([systemOfRecord: "sis", identifier: "123", a: "b"], ConfidenceType.CANONICAL) >> []
        1 * service.databaseService.searchDatabase([systemOfRecord: "sis", identifier: "123", a: "b"], ConfidenceType.POTENTIAL) >> [new Candidate()]

        and: "There are no other calls to the service"
        0 * service.databaseService._(*_)
        0 * service._(*_)
    }

    void "test findExistingRecord"() {
        when:
        service.findExistingRecord([systemOfRecord: "sis", identifier: "123", a: "b"])

        then:
        1 * service.databaseService.findRecord("sis", "123")

        and: "There are no other calls to the service"
        0 * service.databaseService._(*_)
        0 * service._(*_)
    }
}