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

import edu.berkeley.bidms.app.matchservice.PersonPartialMatch
import edu.berkeley.bidms.app.matchservice.config.MatchServiceConfiguration
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.SOR
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.repo.PartialMatchRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import edu.berkeley.bidms.common.json.JsonUtil
import groovy.util.logging.Slf4j
import org.apache.activemq.command.ActiveMQMapMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NewSorConsumerServiceIntegrationSpec extends Specification {

    @LocalServerPort
    int port

    @Autowired
    NewSORConsumerService newSORConsumerService
    @Autowired
    DatabaseService databaseService
    @Autowired
    MatchClientService matchClientService
    @Autowired
    UidClientService uidClientService
    @Autowired
    PersonRepository personRepository
    @Autowired
    SORRepository sorRepository
    @Autowired
    SORObjectRepository sorObjectRepository
    @Autowired
    PartialMatchRepository partialMatchRepository
    @Autowired
    MatchServiceConfiguration matchServiceConfiguration

    def setup() {
        SOR sor
        sor = sorRepository.save(new SOR(name: 'HR'))
        sorObjectRepository.save(new SORObject(
                sor: sor,
                sorPrimaryKey: 'HR0001',
                queryTime: new Date(),
                jsonVersion: 1, objJson: "{}"
        ))
        personRepository.save(new Person(uid: '002'))
        personRepository.save(new Person(uid: '003'))
    }

    def cleanup() {
        partialMatchRepository.deleteAll()
        SOR sor = sorRepository.findByName("HR")
        sorObjectRepository.delete(sorObjectRepository.findBySorAndSorPrimaryKey(sor, "HR0001"))
        sorRepository.delete(sor)
        personRepository.delete(personRepository.get("002"))
        personRepository.delete(personRepository.get("003"))
    }

    static ActiveMQMapMessage createJmsMessage(Map data) {
        ActiveMQMapMessage jmsMsg = new ActiveMQMapMessage()
        data.each { key, value ->
            jmsMsg.setString(key, value)
        }
        return jmsMsg
    }

    @Unroll
    def 'when entering the system with a SORObject that does not match an existing person, expect to see the new created UID on the provisioning queue: #description'() {
        given:
        def data = [systemOfRecord: "HR", sorPrimaryKey: "HR0001", givenName: 'FirstName', surName: 'LastName', dateOfBirth: '1988-01-01']
        def sorObject = sorObjectRepository.findBySorAndSorPrimaryKey(sorRepository.findByName(data.systemOfRecord), data.sorPrimaryKey)

        and: "match engine expectation"
        final mockMatchEngineServer = MockRestServiceServer.createServer((RestTemplate) matchClientService.restTemplate)
        mockMatchEngineServer
                .expect(requestTo(matchServiceConfiguration.restMatchEnginePersonUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(JsonUtil.convertMapToJson([
                        systemOfRecord: "HR",
                        identifier    : "HR0001",
                        dateOfBirth   : "1988-01-01",
                        names         : [[givenName: "FirstName", surName: "LastName", type: "official"]]
                ])))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND))

        and: "provisioning expectation"
        final mockProvisionServer = MockRestServiceServer.createServer((RestTemplate) uidClientService.restTemplate)
        mockProvisionServer
                .expect(requestTo("${matchServiceConfiguration.restProvisionNewUidUrl}?sorObjectId=${sorObject.id}" + (synchronousDownstream ? "&synchronousDownstream=true" : "")))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().string("{\"sorObjectId\":${sorObject.id},\"synchronousDownstream\":true}"))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(JsonUtil.convertMapToJson([uid: '001', sorObjectId: '2', provisioningSuccessful: true]), MediaType.APPLICATION_JSON))

        when:
        Map result = newSORConsumerService.handleMessage(createJmsMessage(data))

        then:
        result.matchType == "noMatch"
        result.uid == "001"
        mockMatchEngineServer.verify()
        mockProvisionServer.verify()

        where:
        description                          | synchronousDownstream
        "provision downstream synchronously" | true
    }


    def 'when entering the system with a SORObject that does match an single existing person, expect to see that persons UID on the provisioning queue'() {
        given:
        def person = personRepository.get('002')
        def data = [systemOfRecord: "HR", sorPrimaryKey: "HR0001", givenName: 'FirstName', surName: 'LastName', dateOfBirth: '1988-01-01']

        and: "match engine expectation"
        final mockMatchEngineServer = MockRestServiceServer.createServer((RestTemplate) matchClientService.restTemplate)
        mockMatchEngineServer.expect(requestTo(matchServiceConfiguration.restMatchEnginePersonUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(JsonUtil.convertMapToJson([
                        systemOfRecord: "HR",
                        identifier    : "HR0001",
                        dateOfBirth   : "1988-01-01",
                        names         : [[givenName: "FirstName", surName: "LastName", type: "official"]]
                ])))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(JsonUtil.convertMapToJson([matchingRecord: [referenceId: '002', ruleNames: ["Canonical #1"]]]), MediaType.APPLICATION_JSON))

        and: "provision expectation"
        final mockProvisionServer = MockRestServiceServer.createServer((RestTemplate) uidClientService.restTemplate)
        mockProvisionServer
                .expect(requestTo("${matchServiceConfiguration.restProvisionUidUrl}?uid=${person.uid}&synchronousDownstream=true"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("{\"synchronousDownstream\":true,\"uid\":\"${person.uid}\"}"))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(JsonUtil.convertMapToJson([uid: '001', sorObjectId: '2', provisioningSuccessful: true]), MediaType.APPLICATION_JSON))

        when:
        Map result = newSORConsumerService.handleMessage(createJmsMessage(data))

        then:
        result.matchType == "exactMatch"
        result.uid == "002"
        mockMatchEngineServer.verify()
        mockProvisionServer.verify()
    }

    def 'when entering the system with a SORObject that matches multiple existing persons, do not expect to see a response on the queue but instead expect to find two rows in the PartialMatch table'() {
        given:
        def data = [systemOfRecord: "HR", sorPrimaryKey: "HR0001", givenName: 'FirstName', surName: 'LastName', dateOfBirth: '1988-01-01']

        and: "match engine expectation"
        final mockMatchEngineServer = MockRestServiceServer.createServer((RestTemplate) matchClientService.restTemplate)
        mockMatchEngineServer.expect(requestTo(matchServiceConfiguration.restMatchEnginePersonUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(JsonUtil.convertMapToJson([
                        systemOfRecord: "HR",
                        identifier    : "HR0001",
                        dateOfBirth   : "1988-01-01",
                        names         : [[givenName: "FirstName", surName: "LastName", type: "official"]]
                ])))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withStatus(HttpStatus.MULTIPLE_CHOICES)
                        .body(
                                JsonUtil.convertMapToJson([
                                        partialMatchingRecords: [
                                                [
                                                        referenceId: '002',
                                                        ruleNames  : ["Potential #1", "Potential #2"]
                                                ],
                                                [
                                                        referenceId: '003',
                                                        ruleNames  : ["Potential #2"]
                                                ]
                                        ]
                                ])
                        )
                        .contentType(MediaType.APPLICATION_JSON))

        when:
        newSORConsumerService.handleMessage(createJmsMessage(data))
        def rows = partialMatchRepository.findAll()

        then:
        mockMatchEngineServer.verify()

        and:
        rows.size() == 2
        rows.collect { it.person.uid }.sort() == ['002', '003']
    }

    def 'when entering the system with a SORObject that does match an single existing person, expect to see all PartialMatches for that SORObject to be deleted'() {
        given:
        def personPartialMatches = [
                new PersonPartialMatch(personRepository.get('002'), ['Potential #1']),
                new PersonPartialMatch(personRepository.get('003'), ['Potential #2'])
        ]
        def data = [systemOfRecord: "HR", sorPrimaryKey: "HR0001", givenName: 'FirstName', surName: 'LastName', dateOfBirth: '1988-01-01']

        and: "match engine expectation"
        final mockMatchEngineServer = MockRestServiceServer.createServer((RestTemplate) matchClientService.restTemplate)
        mockMatchEngineServer.expect(requestTo(matchServiceConfiguration.restMatchEnginePersonUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(JsonUtil.convertMapToJson([
                        systemOfRecord: "HR",
                        identifier    : "HR0001",
                        dateOfBirth   : "1988-01-01",
                        names         : [[givenName: "FirstName", surName: "LastName", type: "official"]]
                ])))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(JsonUtil.convertMapToJson([matchingRecord: [referenceId: '002']]), MediaType.APPLICATION_JSON))

        and: "provision expectation"
        final mockProvisionServer = MockRestServiceServer.createServer((RestTemplate) uidClientService.restTemplate)
        mockProvisionServer
                .expect(requestTo("${matchServiceConfiguration.restProvisionUidUrl}?uid=002&synchronousDownstream=true"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("{\"synchronousDownstream\":true,\"uid\":\"002\"}"))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(JsonUtil.convertMapToJson([uid: '002', provisioningSuccessful: true]), MediaType.APPLICATION_JSON))

        when:
        databaseService.storePartialMatch(sorObjectRepository.findBySorAndSorPrimaryKey(sorRepository.findByName("HR"), "HR0001"), personPartialMatches)
        assert partialMatchRepository.findAll().size() == 2
        Map result = newSORConsumerService.handleMessage(createJmsMessage(data))
        def rows = partialMatchRepository.findAll()

        then:
        result.matchType == "exactMatch"
        result.uid == "002"
        mockMatchEngineServer.verify()
        mockProvisionServer.verify()
        rows.size() == 0
    }
}
