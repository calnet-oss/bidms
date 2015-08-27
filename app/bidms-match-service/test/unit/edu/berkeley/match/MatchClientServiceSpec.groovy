package edu.berkeley.match
import edu.berkeley.match.testutils.TimeoutResponseCreator
import edu.berkeley.registry.model.Person
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.servlet.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.response.DefaultResponseCreator
import org.springframework.web.client.ResourceAccessException
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*
import static org.springframework.test.web.client.response.MockRestResponseCreators.*
/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Mock(Person)
@TestFor(MatchClientService)
class MatchClientServiceSpec extends Specification {


    public static final String UCB_MATCH_URL = 'http://localhost/ucb-match/v1/person'

    def setup() {
        grailsApplication.config.rest = [matchEngine: [url: UCB_MATCH_URL]]
        service.restClient = new RestBuilder()
        createPeople()
    }

    void "test call to match engine where there is no match"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        mockServer.expect(requestTo(UCB_MATCH_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"givenName":"Pat","surName":"Stone","type":"official"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND))

        when:
        def result = service.match([systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone'])

        then:
        mockServer.verify()
        result instanceof PersonNoMatch
    }

    void "test call to match engine where there result is an exact match"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        mockServer.expect(requestTo(UCB_MATCH_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"givenName":"Pat","surName":"Stone","type":"official"}],"identifiers":[{"type":"socialSecurityNumber","identifier":"000-00-0002"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withSuccess(EXACT_MATCH_RESPONSE, MediaType.APPLICATION_JSON))

        when:
        def result = service.match([systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        mockServer.verify()
        result instanceof PersonExactMatch
        result.person.uid == '1'
    }

     void "test call to match engine where there result is a partial match"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        mockServer.expect(requestTo(UCB_MATCH_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"givenName":"Pat","surName":"Stone","type":"official"}],"identifiers":[{"type":"socialSecurityNumber","identifier":"000-00-0002"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond((new DefaultResponseCreator(HttpStatus.MULTIPLE_CHOICES)).body(PARTIAL_MATCH_RESPONSE).contentType(MediaType.APPLICATION_JSON))

        when:
        def result = service.match([systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        mockServer.verify()
        result instanceof PersonPartialMatches
        result.people*.uid == ['1','2']

    }

    void "test call to match engine where the result is an existing record"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        mockServer.expect(requestTo(UCB_MATCH_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"givenName":"Pat","surName":"Stone","type":"official"}],"identifiers":[{"type":"socialSecurityNumber","identifier":"000-00-0002"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond((new DefaultResponseCreator(HttpStatus.FOUND)).body(EXISTING_RECORD_RESPONSE).contentType(MediaType.APPLICATION_JSON))

        when:
        service.match([systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        thrown(RuntimeException)
    }

    void "test call to match engine where it returns a INTERNAL_SERVER_ERROR"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        mockServer.expect(requestTo(UCB_MATCH_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"givenName":"Pat","surName":"Stone","type":"official"}],"identifiers":[{"type":"socialSecurityNumber","identifier":"000-00-0002"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withServerError())

        when:
        service.match([systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        thrown(RuntimeException)
    }

    void "test call to match engine where there match engine times out"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        mockServer.expect(requestTo(UCB_MATCH_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"givenName":"Pat","surName":"Stone","type":"official"}],"identifiers":[{"type":"socialSecurityNumber","identifier":"000-00-0002"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(TimeoutResponseCreator.withTimeout())

        when:
        service.match([systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        thrown(ResourceAccessException)
    }


    static EXACT_MATCH_RESPONSE = '{"matchingRecord":{"referenceId":"1"}}'
    static PARTIAL_MATCH_RESPONSE = '{"partialMatchingRecords":[{"referenceId":"1"},{"referenceId":"2"}]}'
    static EXISTING_RECORD_RESPONSE = '{"matchingRecord":{"referenceId":"1"}}'

    void createPeople() {
        ['1','2'].each {
            new Person(uid: it).save(validate: false)
        }
        assert Person.count() == 2
    }

}
