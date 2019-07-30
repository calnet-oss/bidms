package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.database.Candidate
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class MatchServiceSpec extends Specification implements ServiceUnitTest<MatchService>, DataTest {
    static transactional = false

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
    void "test findCandidates where superCanonical and canonical do not returns a match"() {
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

