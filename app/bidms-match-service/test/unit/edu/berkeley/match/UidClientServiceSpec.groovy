package edu.berkeley.match

import edu.berkeley.match.testutils.TimeoutResponseCreator
import edu.berkeley.registry.model.Person
import grails.buildtestdata.mixin.Build
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.servlet.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.ResourceAccessException
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(UidClientService)
@Build([Person])
class UidClientServiceSpec extends Specification {
    private static final UCB_UID_SERVICE_URL = 'http://localhost/ucb-uid-service/nextUid'
    private static final INPUT_ATTRIBUTE_MAP = [systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone']
    private static final EXPECTED_REST_INPUT = '{"systemOfRecord":"b","sorPrimaryKey":"BB00002","dateOfBirth":"1930-04-20","givenName":"Pat","surName":"Stone"}'

    def setup() {
        grailsApplication.config.rest = [uidService: [url: UCB_UID_SERVICE_URL]]
        service.restClient = new RestBuilder()
    }


    void "when requesting a new UID for a Person, the UID service is called and the Person is persisted with that UID in the DB"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        mockServer.expect(requestTo(UCB_UID_SERVICE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(EXPECTED_REST_INPUT))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withSuccess("{'uid': '123'}", MediaType.APPLICATION_JSON))
        Person.build(uid: '123', givenName: 'Pat', surName: 'Stone', dateOfBirth: '1930-04-20') // Mock person that the uid-service would create

        when:
        def result = service.createUidForPerson(INPUT_ATTRIBUTE_MAP)

        then:
        mockServer.verify()
        result instanceof Person
        result.uid == '123'
        result.givenName == 'Pat'
        result.surName == 'Stone'
        result.dateOfBirth == '1930-04-20'
    }

    void "when requesting a new UID for a Person and the server returns a not-OK answer the service call throws an exception"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        mockServer.expect(requestTo(UCB_UID_SERVICE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(EXPECTED_REST_INPUT))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withNoContent())

        when:
        service.createUidForPerson(INPUT_ATTRIBUTE_MAP)

        then:
        thrown(RuntimeException)
    }

    void "when requesting a new UID for a Person and the server times out an exception is thrown"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        mockServer.expect(requestTo(UCB_UID_SERVICE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(EXPECTED_REST_INPUT))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(TimeoutResponseCreator.withTimeout())

        when:
        service.createUidForPerson(INPUT_ATTRIBUTE_MAP)

        then:
        thrown(ResourceAccessException)
    }
}
