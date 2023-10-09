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

import edu.berkeley.bidms.app.common.config.properties.BidmsConfigProperties
import edu.berkeley.bidms.app.matchservice.PersonExactMatch
import edu.berkeley.bidms.app.matchservice.PersonExistingMatch
import edu.berkeley.bidms.app.matchservice.PersonNoMatch
import edu.berkeley.bidms.app.matchservice.PersonPartialMatches
import edu.berkeley.bidms.app.matchservice.config.MatchServiceConfiguration
import edu.berkeley.bidms.app.matchservice.rest.MatchEngineRestTemplate
import edu.berkeley.bidms.app.matchservice.testutils.TimeoutResponseCreator
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.restclient.service.MatchEngineRestClientService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.ResourceAccessException
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class MatchClientServiceSpec extends Specification {

    @Autowired
    BidmsConfigProperties bidmsConfigProperties
    @Autowired
    MatchServiceConfiguration matchServiceConfiguration
    @Autowired
    MatchEngineRestTemplate restTemplate
    @Autowired
    PersonRepository personRepository

    MatchEngineRestClientService matchEngineRestClientService
    MatchClientService service

    def setup() {
        this.matchEngineRestClientService = new MatchEngineRestClientService(bidmsConfigProperties)
        this.service = new MatchClientService(restTemplate, matchEngineRestClientService, personRepository)
        createPeople()
    }

    void "test call to match engine where there is no match"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(restTemplate)
        mockServer.expect(requestTo(matchServiceConfiguration.restMatchEnginePersonUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"givenName":"Pat","surName":"Stone","type":"official"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND))

        when:
        def result = service.match('eventId', [systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone'])

        then:
        mockServer.verify()
        result instanceof PersonNoMatch
        !((PersonNoMatch) result).matchOnly
    }

    void "test call to match engine where there is no match and matchOnly flag is set true"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(restTemplate)
        mockServer.expect(requestTo(matchServiceConfiguration.restMatchEnginePersonUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","matchOnly":true,"names":[{"givenName":"Pat","surName":"Stone","type":"official"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND))

        when:
        def result = service.match('eventId', [systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', matchOnly: true])

        then:
        mockServer.verify()
        result instanceof PersonNoMatch
        ((PersonNoMatch) result).matchOnly
    }

    void "test call to match engine where there result is an exact match"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(restTemplate)
        mockServer.expect(requestTo(matchServiceConfiguration.restMatchEnginePersonUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"givenName":"Pat","surName":"Stone","type":"official"}],"identifiers":[{"type":"socialSecurityNumber","identifier":"000-00-0002"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(EXACT_MATCH_RESPONSE, MediaType.APPLICATION_JSON))

        when:
        PersonExactMatch result = service.match('eventId', [systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        mockServer.verify()
        result.person.uid == '1'
        result.ruleNames == ['Canonical #1']
    }

    void "test call to match engine where there result is a partial match"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(restTemplate)
        mockServer.expect(requestTo(matchServiceConfiguration.restMatchEnginePersonUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"givenName":"Pat","surName":"Stone","type":"official"}],"identifiers":[{"type":"socialSecurityNumber","identifier":"000-00-0002"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond((withStatus(HttpStatus.MULTIPLE_CHOICES)).body(PARTIAL_MATCH_RESPONSE).contentType(MediaType.APPLICATION_JSON))

        when:
        PersonPartialMatches result = service.match('eventId', [systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        mockServer.verify()
        result.partialMatches.person.uid == ['1', '2']
        result.partialMatches.ruleNames == [['Potential #1'], ['Potential #2']]

    }

    void "test call to match engine where the result is an existing record"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(restTemplate)
        mockServer.expect(requestTo(matchServiceConfiguration.restMatchEnginePersonUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"givenName":"Pat","surName":"Stone","type":"official"}],"identifiers":[{"type":"socialSecurityNumber","identifier":"000-00-0002"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond((withStatus(HttpStatus.FOUND)).body(EXISTING_RECORD_RESPONSE).contentType(MediaType.APPLICATION_JSON))

        when:
        def result = service.match('eventId', [systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        mockServer.verify()
        result instanceof PersonExistingMatch
        result.person.uid == '1'
    }

    void "test call to match engine where it returns a INTERNAL_SERVER_ERROR"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(restTemplate)
        mockServer.expect(requestTo(matchServiceConfiguration.restMatchEnginePersonUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"givenName":"Pat","surName":"Stone","type":"official"}],"identifiers":[{"type":"socialSecurityNumber","identifier":"000-00-0002"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withServerError())

        when:
        service.match('eventId', [systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        thrown(RuntimeException)
    }

    void "test call to match engine where there match engine times out"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(restTemplate)
        mockServer.expect(requestTo(matchServiceConfiguration.restMatchEnginePersonUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string('{"systemOfRecord":"b","identifier":"BB00002","dateOfBirth":"1930-04-20","names":[{"givenName":"Pat","surName":"Stone","type":"official"}],"identifiers":[{"type":"socialSecurityNumber","identifier":"000-00-0002"}]}'))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(TimeoutResponseCreator.withTimeout())

        when:
        service.match('eventId', [systemOfRecord: 'b', sorPrimaryKey: 'BB00002', dateOfBirth: '1930-04-20', givenName: 'Pat', surName: 'Stone', socialSecurityNumber: '000-00-0002'])

        then:
        thrown(ResourceAccessException)
    }

    static EXACT_MATCH_RESPONSE = '{"matchingRecord":{"exactMatch":true,"referenceId":"1","ruleNames":["Canonical #1"]}}'
    static PARTIAL_MATCH_RESPONSE = ' {"partialMatchingRecords":[{"exactMatch":false,"referenceId":"1","ruleNames":["Potential #1"]},{"exactMatch":false,"referenceId":"2","ruleNames":["Potential #2"]}]}}'
    static EXISTING_RECORD_RESPONSE = '{"matchingRecord":{"exactMatch":true,"referenceId":"1","ruleNames":[]}}'

    void createPeople() {
        ['1', '2'].each {
            personRepository.saveAndFlush(new Person(uid: it))
        }
        assert personRepository.count() == 2
    }
}
