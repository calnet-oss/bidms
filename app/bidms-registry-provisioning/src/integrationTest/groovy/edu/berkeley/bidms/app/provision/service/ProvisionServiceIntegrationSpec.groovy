/*
 * Copyright (c) 2021, Regents of the University of California and
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
package edu.berkeley.bidms.app.provision.service

import edu.berkeley.bidms.app.jmsclient.service.ProvisioningJmsClientService
import edu.berkeley.bidms.app.registryModel.model.NameType
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.PersonName
import edu.berkeley.bidms.app.registryModel.model.PersonSorObjectsJson
import edu.berkeley.bidms.app.registryModel.model.PersonSorObjectsJsonWriteable
import edu.berkeley.bidms.app.registryModel.model.SOR
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.model.type.NameTypeEnum
import edu.berkeley.bidms.app.registryModel.repo.NameTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonNameRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonSorObjectsJsonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import edu.berkeley.bidms.orm.transaction.JpaTransactionTemplate
import edu.berkeley.bidms.provision.common.ProvisionRunner
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
class ProvisionServiceIntegrationSpec extends Specification {

    @Autowired
    ProvisionService provisionService

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    PersonSorObjectsJsonRepository personSorObjectsJsonRepository

    @Autowired
    NameTypeRepository nameTypeRepository

    @Autowired
    PersonNameRepository personNameRepository

    @Autowired
    PlatformTransactionManager transactionManager

    JpaTransactionTemplate transactionTemplate

    void setup() {
        this.transactionTemplate = new JpaTransactionTemplate(transactionManager, TransactionDefinition.PROPAGATION_REQUIRED)

        provisionService.provisionRunnerService = Mock(ProvisionRunner)
        provisionService.downstreamProvisioningService = Mock(AbstractDownstreamProvisioningService)
        provisionService.provisioningJmsClientService = Mock(ProvisioningJmsClientService)

        Person person = personRepository.saveAndFlush(new Person(uid: "1"))

        SOR sor = sorRepository.saveAndFlush(new SOR(name: "TEST_SOR"))
        nameTypeRepository.saveAndFlush(new NameType(typeName: NameTypeEnum.sorPrimaryName.name))

        SORObject sorObject
        JSONObject objJsonMap = [
                NAMES: [
                        [
                                FIRST_NAME: "John",
                                LAST_NAME : "Smith"
                        ]
                ]
        ] as JSONObject
        String objJsonStr = objJsonMap.toString(2)
        sorObject = new SORObject(
                sor: sor,
                sorPrimaryKey: "so123",
                uid: "1",
                queryTime: new Date(),
                objJson: objJsonStr,
                jsonVersion: 1
        )
        sorObjectRepository.saveAndFlush(sorObject)

        JSONObject aggregateJsonMap = [
                uid       : "1",
                sorObjects: [
                        [
                                id       : sorObject.id,
                                uid      : sorObject.uid,
                                sorId    : sorObject.sor.id,
                                sorName  : sorObject.sor.name,
                                sorObjKey: sorObject.sorPrimaryKey,
                                objJson  : objJsonMap
                        ]
                ]
        ] as JSONObject
        String aggregateJsonStr = aggregateJsonMap.toString(2)
        PersonSorObjectsJson psoj = new PersonSorObjectsJsonWriteable(id: "1")
        psoj.with {
            lastUpdated = new Date()
            aggregateJson = aggregateJsonStr
            jsonHash = Integer.toString(aggregateJsonStr.hashCode())
        }
        personSorObjectsJsonRepository.saveAndFlush(psoj)
    }

    void cleanup() {
        transactionTemplate.executeWithoutResult {
            personSorObjectsJsonRepository.delete(personSorObjectsJsonRepository.get("1"))

            Person person = personRepository.get("1")
            personNameRepository.deleteAll(person.names)

            SOR sor = sorRepository.findByName("TEST_SOR")
            sorObjectRepository.delete(sorObjectRepository.findBySorAndSorPrimaryKey(sor, "so123"))
            sorRepository.delete(sor)

            personRepository.delete(person)
            nameTypeRepository.delete(nameTypeRepository.findByTypeName(NameTypeEnum.sorPrimaryName.name))
        }
    }

    @Unroll
    void "test provisionUid"() {
        given:
        String uid = "1"

        when: "person is reprovisioned"
        Map result = provisionService.provisionUid(uid, synchronousDownstream, "eventId")
        PersonName name = transactionTemplate.execute {
            personRepository.get("1").names.find {
                it.sorObject.sor.name == "TEST_SOR" && it.sorObject.sorPrimaryKey == "so123"
            }
        }

        then: "provision runner is called to rebuild the person"
        result.uid == "1"
        1 * provisionService.provisionRunnerService.run(_, _) >> { Person p, Map sorPerson ->
            Map objJson = ((List<Map>) sorPerson.sorObjects).find { it.sorName == "TEST_SOR" }?.objJson
            Map nameJson = ((List<Map>) objJson.NAMES).first()
            p.addToNames(new PersonName(p).with {
                it.nameType = nameTypeRepository.findByTypeName(NameTypeEnum.sorPrimaryName.name)
                it.sorObject = sorObjectRepository.findBySorAndSorPrimaryKey(sorRepository.findByName("TEST_SOR"), "so123")
                it.givenName = nameJson.FIRST_NAME
                it.surName = nameJson.LAST_NAME
                it.fullName = "${nameJson.FIRST_NAME} ${nameJson.LAST_NAME}"
                it
            })
            [result: "success"]
        }
        (synchronousDownstream ? 1 : 0) * provisionService.downstreamProvisioningService.provisionUidSynchronously(*_)
        (!synchronousDownstream ? 1 : 0) * provisionService.downstreamProvisioningService.provisionUidAsynchronously(*_)

        and: "person has a name provisioned"
        name.fullName == "John Smith"

        where:
        synchronousDownstream | _
        false                 | _
        true                  | _
    }
}
