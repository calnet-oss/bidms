package edu.berkeley.match
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.servlet.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.web.client.MockRestServiceServer
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(MatchClientService)
class MatchClientServiceSpec extends Specification {


    public static final String UCB_MATCH_URL = 'http://localhost/ucb-match/v1/people'

    def setup() {
        grailsApplication.config.match = [ucbMatchUrl: UCB_MATCH_URL]
        service.restClient = new RestBuilder()
    }


    void "test call to match engine where there is no match"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        mockServer.expect(requestTo(UCB_MATCH_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"type":"official","given":"Pat","family":"Stone"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND))

        when:
        def result = service.match([systemOfRecord: 'b', sorIdentifier: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', familyName: 'Stone'])

        then:
        mockServer.verify()
        result instanceof NoMatch
    }
}
