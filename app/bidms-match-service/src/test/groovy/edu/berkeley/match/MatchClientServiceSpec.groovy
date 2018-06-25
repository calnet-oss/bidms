package edu.berkeley.match

import edu.berkeley.match.testutils.TimeoutResponseCreator
import edu.berkeley.registry.model.Person
import grails.plugins.rest.client.RestBuilder
import grails.web.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.response.DefaultResponseCreator
import org.springframework.web.client.ResourceAccessException
import spock.lang.Specification
import grails.testing.gorm.DomainUnitTest
import grails.testing.services.ServiceUnitTest

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*
import static org.springframework.test.web.client.response.MockRestResponseCreators.*

class MatchClientServiceSpec extends Specification implements ServiceUnitTest<MatchClientService>, DomainUnitTest<Person> {

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
        !((PersonNoMatch) result).matchOnly
    }

    void "test call to match engine where there is no match and matchOnly flag is set true"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        mockServer.expect(requestTo(UCB_MATCH_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","matchOnly":true,"names":[{"givenName":"Pat","surName":"Stone","type":"official"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND))

        when:
        def result = service.match([systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', matchOnly: true])

        then:
        mockServer.verify()
        result instanceof PersonNoMatch
        ((PersonNoMatch) result).matchOnly
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
        PersonExactMatch result = service.match([systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        mockServer.verify()
        result.person.uid == '1'
        result.ruleNames == ['Canonical #1']
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
        PersonPartialMatches result = service.match([systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        mockServer.verify()
        result.partialMatches.person.uid == ['1', '2']
        result.partialMatches.ruleNames == [['Potential #1'], ['Potential #2']]

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
        def result = service.match([systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        mockServer.verify()
        result instanceof PersonExistingMatch
        result.person.uid == '1'
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


    static EXACT_MATCH_RESPONSE = '{"matchingRecord":{"exactMatch":true,"referenceId":"1","ruleNames":["Canonical #1"]}}'
    static PARTIAL_MATCH_RESPONSE = ' {"partialMatchingRecords":[{"exactMatch":false,"referenceId":"1","ruleNames":["Potential #1"]},{"exactMatch":false,"referenceId":"2","ruleNames":["Potential #2"]}]}}'
    static EXISTING_RECORD_RESPONSE = '{"matchingRecord":{"exactMatch":true,"referenceId":"1","ruleNames":[]}}'

    void createPeople() {
        ['1', '2'].each {
            new Person(uid: it).save(validate: false)
        }
        assert Person.count() == 2
    }

}
