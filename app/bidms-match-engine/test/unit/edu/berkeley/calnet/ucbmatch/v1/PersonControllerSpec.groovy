package edu.berkeley.calnet.ucbmatch.v1

import edu.berkeley.calnet.ucbmatch.PersonService
import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.response.ExactMatchResponse
import grails.test.mixin.TestFor
import org.springframework.http.HttpStatus
import spock.lang.Specification

@TestFor(PersonController)
class PersonControllerSpec extends Specification {

    def setup() {
        controller.personService = Mock(PersonService)
    }

    def "test that get person returns the expected json"() {
        setup:
        request.json = [input1: 'xxx']

        when:
        controller.getPerson()

        then:
        1 * controller.personService.matchPerson([input1: 'xxx']) >> new ExactMatchResponse(responseData: new Candidate(referenceId: '1', systemOfRecord: 'HR'))

        and:
        response.status == HttpStatus.OK.value()
        response.json.matchingRecord.systemOfRecord=='HR'
        response.json.matchingRecord.referenceId=='1'
    }
}
