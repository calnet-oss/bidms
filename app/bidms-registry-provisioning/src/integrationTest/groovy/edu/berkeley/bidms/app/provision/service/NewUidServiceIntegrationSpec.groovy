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
package edu.berkeley.bidms.app.provision.service

import edu.berkeley.bidms.app.registryModel.model.SOR
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.model.type.MatchHistoryResultTypeEnum
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import edu.berkeley.bidms.app.registryModel.repo.history.MatchHistoryRepository
import groovy.sql.Sql
import jakarta.persistence.EntityManager
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Unroll

import javax.sql.DataSource

@SpringBootTest
class NewUidServiceIntegrationSpec extends Specification {
    @Autowired
    EntityManager entityManager

    @Autowired
    DataSource dataSource

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    NewUidService newUidService

    @Autowired
    MatchHistoryRepository matchHistoryRepository

    void setup() {
        // Create uid_seq in our test DB which keeps track of last uid assigned.
        Sql sql = new Sql(dataSource)
        sql.execute("CREATE SEQUENCE uid_seq START WITH 1 INCREMENT BY 1" as String)
        sql.close()

        SOR sor = sorRepository.saveAndFlush(new SOR(name: "TEST_SOR"))

        SORObject sorObject
        JSONObject objJsonMap = [:] as JSONObject
        String objJsonStr = objJsonMap.toString(2)
        sorObject = new SORObject(
                sor: sor,
                sorPrimaryKey: "new123",
                queryTime: new Date(),
                objJson: objJsonStr,
                jsonVersion: 1
        )
        sorObjectRepository.saveAndFlush(sorObject)
    }

    void cleanup() {
        SOR sor = sorRepository.findByName("TEST_SOR")
        sorObjectRepository.delete(sorObjectRepository.findBySorAndSorPrimaryKey(sor, "new123"))
        sorRepository.delete(sor)
        matchHistoryRepository.deleteAll()
        Sql sql = new Sql(dataSource)
        sql.execute("DROP SEQUENCE uid_seq" as String)
        sql.close()
    }

    @Unroll
    void "test provisionNewUid"() {
        given: "an unassigned SORObject"
        SOR sor = sorRepository.findByName("TEST_SOR")
        SORObject sorObject = sorObjectRepository.findBySorAndSorPrimaryKey(sor, "new123")
        assert !sorObject.uid

        and: "a mock provisionService"
        newUidService.provisionService = Mock(ProvisionService)

        when: "assign a new uid"
        NewUidService.NewUidResult result = newUidService.provisionNewUid(sorObject.id, synchronousDownstream, "eventId")
        sorObject = sorObjectRepository.findBySorAndSorPrimaryKey(sor, "new123")

        and: "retrieve match history"
        def matchHistories = matchHistoryRepository.findBySorObjectIdAndMatchResultType(sorObject.id, MatchHistoryResultTypeEnum.NEW_UID)

        then: "sorObject now has a uid"
        result.uidGenerationSuccessful && result.uid
        sorObject.uid == result.uid

        and: "uid has been reprovisioned"
        1 * newUidService.provisionService.provisionUid(_, synchronousDownstream, "eventId") >> { String uid, Boolean _synchronousDownstream, String eventId ->
            [message: "success", uid: uid]
        }

        and: "the new uid has a Person row"
        personRepository.get(sorObject.uid)

        and: "there is a match history row"
        matchHistories.size() == 1
        with(matchHistories[0]) {
            uidAssigned == result.uid
            sorObjectId == sorObject.id
            sorId == sorObject.sor.id
            sorPrimaryKey == sorObject.sorPrimaryKey
            eventId == "eventId"
        }

        where:
        synchronousDownstream | _
        false                 | _
        true                  | _
    }
}
