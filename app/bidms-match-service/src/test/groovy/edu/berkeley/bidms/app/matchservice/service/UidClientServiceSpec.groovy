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
import edu.berkeley.bidms.app.matchservice.config.MatchServiceConfiguration
import edu.berkeley.bidms.app.matchservice.rest.ProvisionRestTemplate
import edu.berkeley.bidms.app.matchservice.testutils.TimeoutResponseCreator
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.SOR
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.repo.PersonSorObjectsJsonRepository
import edu.berkeley.bidms.app.restclient.service.ProvisionRestClientService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.ResourceAccessException
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class UidClientServiceSpec extends Specification {

    @Autowired
    BidmsConfigProperties bidmsConfigProperties
    @Autowired
    MatchServiceConfiguration matchServiceConfiguration
    @Autowired
    ProvisionRestTemplate restTemplate
    @Autowired
    PersonSorObjectsJsonRepository personSorObjectsJsonRepository

    ProvisionRestClientService provisionRestClientService
    UidClientService service

    def setup() {
        this.provisionRestClientService = new ProvisionRestClientService(bidmsConfigProperties)
        this.service = new UidClientService(restTemplate, provisionRestClientService, personSorObjectsJsonRepository)
    }

    @Unroll
    void "provision a new uid and #description and return a success"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(restTemplate)
        SORObject sorObject = new SORObject(id: 1, sor: new SOR(name: "TEST"))
        mockServer.expect(requestTo("${matchServiceConfiguration.restProvisionNewUidUrl}?sorObjectId=${sorObject.id}" + (synchronousDownstream ? "&synchronousDownstream=true" : "")))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().string(synchronousDownstream ? "{\"sorObjectId\":${sorObject.id},\"synchronousDownstream\":true}" : "{\"sorObjectId\":${sorObject.id}}"))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('{"provisioningSuccessful": true}', MediaType.APPLICATION_JSON))

        when:
        service.provisionNewUid(sorObject, synchronousDownstream)

        then:
        mockServer.verify()

        where:
        description                           | synchronousDownstream
        "provision downstream synchronously"  | true
        "provision downstream asynchronously" | false
    }

    void "provisioning a new uid server times out and exception is thrown"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(restTemplate)
        SORObject sorObject = new SORObject(id: 1)
        mockServer.expect(requestTo("${matchServiceConfiguration.restProvisionNewUidUrl}?sorObjectId=${sorObject.id}&synchronousDownstream=true"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().string("{\"sorObjectId\":${sorObject.id},\"synchronousDownstream\":true}"))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(TimeoutResponseCreator.withTimeout())

        when:
        service.provisionNewUid(sorObject)

        then:
        thrown(ResourceAccessException)
    }

    void "provision an existing uid and return a success"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(restTemplate)
        Person person = new Person(uid: "1")
        mockServer.expect(requestTo("${matchServiceConfiguration.restProvisionUidUrl}?uid=${person.uid}&synchronousDownstream=true"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("{\"uid\":\"${person.uid}\",\"synchronousDownstream\":true}"))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess())

        when:
        service.provisionUid(person)

        then:
        mockServer.verify()
    }

    void "provisioning an existing uid server times out and exception is thrown"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(restTemplate)
        Person person = new Person(uid: "1")
        mockServer.expect(requestTo("${matchServiceConfiguration.restProvisionUidUrl}?uid=${person.uid}&synchronousDownstream=true"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("{\"uid\":\"${person.uid}\",\"synchronousDownstream\":true}"))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(TimeoutResponseCreator.withTimeout())

        when:
        service.provisionUid(person)

        then:
        thrown(ResourceAccessException)
    }
}
