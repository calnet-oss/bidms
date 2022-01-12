/*
 * Copyright (c) 2018, Regents of the University of California and
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
package edu.berkeley.bidms.app.provision.controller

import edu.berkeley.bidms.app.provision.test.TestUtil
import edu.berkeley.bidms.app.registryModel.repo.auth.RegistryRoleRepository
import edu.berkeley.bidms.app.registryModel.repo.auth.RegistryUserRepository
import edu.berkeley.bidms.app.springsecurity.service.RegistryUserCredentialService
import edu.berkeley.bidms.orm.transaction.JpaTransactionTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NewUidControllerIntegrationSpec extends Specification {

    @LocalServerPort
    int port

    @Autowired
    PlatformTransactionManager transactionManager

    @Autowired
    RegistryUserRepository registryUserRepository

    @Autowired
    RegistryRoleRepository registryRoleRepository

    @Autowired
    RestTemplateBuilder restTemplateBuilder

    @Autowired
    RegistryUserCredentialService registryUserCredentialService

    TestRestTemplate restTemplate
    JpaTransactionTemplate newTransactionTemplate

    void setup() {
        this.restTemplate = new TestRestTemplate(restTemplateBuilder, "testuser", "testpassword")
        this.newTransactionTemplate = new JpaTransactionTemplate(transactionManager, TransactionDefinition.PROPAGATION_REQUIRES_NEW)
    }

    void "test that the NewUidController is accepting requests"() {
        when:
        newTransactionTemplate.executeWithoutResult {
            TestUtil.addTestUser(registryUserCredentialService, registryUserRepository, registryRoleRepository)
        }

        def response = restTemplate.exchange(
                "http://localhost:${port}/registry-provisioning/newUid/save",
                HttpMethod.PUT,
                new HttpEntity<Map>([sorObjectId: 123], new HttpHeaders(contentType: MediaType.APPLICATION_JSON, accept: [MediaType.APPLICATION_JSON])),
                Map
        )

        Map json = (response.statusCode == HttpStatus.OK ? response.body : null)

        and: "cleanup"
        newTransactionTemplate.executeWithoutResult {
            TestUtil.deleteTestUser(registryUserRepository, registryRoleRepository)
        }

        then:
        response.statusCode == HttpStatus.OK
        json.provisioningErrorMessage == "Couldn't find sorObjectId=123"
        !json.provisioningSuccessful
    }
}
