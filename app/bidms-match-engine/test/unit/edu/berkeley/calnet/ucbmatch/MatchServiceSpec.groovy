package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.database.Candidate
import grails.test.mixin.TestFor
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(MatchService)
class MatchServiceSpec extends Specification {
    static transactional = false

    def setup() {
        service.databaseService = Mock(DatabaseService)
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    void "test findCandidates where canonical returns a match"() {
        when:
        service.findCandidates([systemOfRecord: "sis", identifier: "123",a:"b"])

        then:
        1 * service.databaseService.searchDatabase([systemOfRecord: "sis", identifier: "123",a:"b"],ConfidenceType.CANONICAL) >> [new Candidate()]

        and: "There are no other calls to the service"
        0 * service.databaseService._(*_)
        0 * service._(*_)
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    void "test findCandidates where canonical does not returns a match"() {
        when:
        service.findCandidates([systemOfRecord: "sis", identifier: "123",a:"b"])

        then:
        1 * service.databaseService.searchDatabase([systemOfRecord: "sis", identifier: "123",a:"b"],ConfidenceType.CANONICAL) >> []
        1 * service.databaseService.searchDatabase([systemOfRecord: "sis", identifier: "123",a:"b"],ConfidenceType.POTENTIAL) >> [new Candidate()]

        and: "There are no other calls to the service"
        0 * service.databaseService._(*_)
        0 * service._(*_)
    }

    void "test findExistingRecord"() {
        when:
        service.findExistingRecord([systemOfRecord: "sis", identifier: "123",a:"b"])

        then:
        1 * service.databaseService.findRecord("sis", "123")

        and: "There are no other calls to the service"
        0 * service.databaseService._(*_)
        0 * service._(*_)
    }

}

