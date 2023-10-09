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
package edu.berkeley.bidms.app.matchservice.controller

import edu.berkeley.bidms.app.matchservice.config.MatchServiceConfiguration
import edu.berkeley.bidms.app.matchservice.service.NewSORConsumerService
import edu.berkeley.bidms.app.registryModel.model.SOR
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TriggerMatchControllerIntegrationSpec extends Specification {

    @LocalServerPort
    int port

    TestRestTemplate restTemplate

    @Autowired
    TriggerMatchController controller
    @Autowired
    MatchServiceConfiguration matchServiceConfiguration
    @Autowired
    SORObjectRepository sorObjectRepository
    @Autowired
    SORRepository sorRepository
    @Autowired
    RestTemplateBuilder restTemplateBuilder

    def setup() {
        this.restTemplate = new TestRestTemplate(restTemplateBuilder)
        controller.newSORConsumerService = Mock(NewSORConsumerService)
        sorObjectRepository.save(new SORObject(
                sor: sorRepository.save(new SOR(name: 'SIS')),
                sorPrimaryKey: '12345',
                objJson: '{}',
                jsonVersion: 1,
                queryTime: new Date()
        ))
    }

    def cleanup() {
        def sor = sorRepository.findByName('SIS')
        sorObjectRepository.delete(sorObjectRepository.findBySorAndSorPrimaryKey(sor, '12345'))
        sorRepository.delete(sor)
    }

    @Unroll
    void "test that expected path is chosen in controller, depending on input"() {
        given:
        Map<String, Object> attrMap = [
                systemOfRecord: 'SIS',
                sorPrimaryKey : sorPk,
                givenName     : 'Kryf',
                surName       : 'Plyf'
        ]

        when:
        def response = restTemplate.exchange(
                "http://localhost:${port}/match-service/internal/api/trigger-match",
                HttpMethod.POST,
                new HttpEntity<Map>(attrMap, new HttpHeaders(contentType: MediaType.APPLICATION_JSON, accept: [MediaType.APPLICATION_JSON])),
                Map
        )

        then:
        serviceCallCount * controller.newSORConsumerService.matchPerson(_ as String, _ as SORObject, attrMap, true)
        response.statusCode == expectedStatus

        where:
        sorPk   | serviceCallCount | expectedStatus
        null    | 0                | HttpStatus.BAD_REQUEST
        '54321' | 0                | HttpStatus.BAD_REQUEST
        '12345' | 1                | HttpStatus.OK
    }
}
