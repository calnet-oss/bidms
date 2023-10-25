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

import edu.berkeley.bidms.app.matchservice.PersonExactMatch
import edu.berkeley.bidms.app.matchservice.PersonExistingMatch
import edu.berkeley.bidms.app.matchservice.PersonNoMatch
import edu.berkeley.bidms.app.matchservice.PersonPartialMatch
import edu.berkeley.bidms.app.matchservice.PersonPartialMatches
import edu.berkeley.bidms.app.registryModel.model.PartialMatch
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.SOR
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.model.type.MatchHistoryResultTypeEnum
import edu.berkeley.bidms.app.registryModel.repo.PartialMatchRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import edu.berkeley.bidms.app.registryModel.repo.history.MatchHistoryRepository
import edu.berkeley.bidms.common.json.JsonUtil
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification
import spock.lang.Unroll

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class DatabaseServiceSpec extends Specification {

    @Autowired
    EntityManager entityManager
    @Autowired
    SORRepository sorRepository
    @Autowired
    SORObjectRepository sorObjectRepository
    @Autowired
    PersonRepository personRepository
    @Autowired
    PartialMatchRepository partialMatchRepository
    @Autowired
    MatchHistoryRepository matchHistoryRepository

    DatabaseService service
    SORObject sorObject
    PartialMatch existingPartialMatch
    Person person1
    Person person2

    def setup() {
        this.service = new DatabaseService(entityManager, sorObjectRepository, partialMatchRepository, matchHistoryRepository)

        SOR sor = sorRepository.saveAndFlush(new SOR(name: 'SIS'))
        this.sorObject = sorObjectRepository.saveAndFlush(new SORObject(
                sor: sor,
                sorPrimaryKey: 'SIS123',
                queryTime: new Date(),
                objJson: "{}",
                jsonVersion: 1
        ))
        this.person1 = personRepository.saveAndFlush(new Person(uid: "1"))
        this.person2 = personRepository.saveAndFlush(new Person(uid: "2"))

        def epm = new PartialMatch(person1)
        epm.sorObject = sorObject
        this.existingPartialMatch = partialMatchRepository.saveAndFlush(epm)
    }

    void "when assigning a uid to a SORObject it will be persisted"() {
        expect:
        sorObject.person == null

        when:
        service.assignUidToSOR(sorObject, person1)

        then:
        sorObject.person == person1
    }

    void "when storing partial match not previously exsting a new PartialMatch is created"() {
        given:
        def sorObject2 = sorObjectRepository.saveAndFlush(new SORObject(
                sor: sorRepository.save(new SOR(name: 'HR')),
                sorPrimaryKey: 'HR123',
                queryTime: new Date(),
                objJson: "{}",
                jsonVersion: 1
        ))
        def person3 = personRepository.saveAndFlush(new Person(uid: '3'))

        expect:
        partialMatchRepository.countBySorObject(sorObject2) == 0

        when:
        service.storePartialMatch(sorObject2, [createPersonPartialMatch("Potential #2", person2), createPersonPartialMatch("Potential #3", person3)])

        then:
        partialMatchRepository.countBySorObject(sorObject2) == 2
        partialMatchRepository.countBySorObjectAndPerson(sorObject2, person2) == 1
        partialMatchRepository.countBySorObjectAndPerson(sorObject2, person3) == 1
        with(partialMatchRepository.findBySorObjectAndPerson(sorObject2, person2)) {
            metaData.ruleNames == ['Potential #2']
        }
        with(partialMatchRepository.findBySorObjectAndPerson(sorObject2, person3)) {
            metaData.ruleNames == ['Potential #3']
        }
    }

    void "when storing partial match on existing PartialMatch, where there was only one match the correct update takes place"() {
        expect:
        partialMatchRepository.countBySorObject(sorObject) == 1

        when:
        service.storePartialMatch(sorObject, [createPersonPartialMatch("Potential #1", person1), createPersonPartialMatch("Potential #2", person2)])

        then:
        partialMatchRepository.countBySorObject(sorObject) == 2
        partialMatchRepository.countBySorObjectAndPerson(sorObject, person1) == 1
        partialMatchRepository.countBySorObjectAndPerson(sorObject, person2) == 1
        with(partialMatchRepository.findBySorObjectAndPerson(sorObject, person1)) {
            metaData.ruleNames == ['Potential #1']
        }
        with(partialMatchRepository.findBySorObjectAndPerson(sorObject, person2)) {
            metaData.ruleNames == ['Potential #2']
        }
    }

    void "when assigning a new uid to a SORObject in the PartialMatch table, confirm the PartialMatch is removed"() {
        expect: "That there are exactly one partialMatch record for the sorObject"
        partialMatchRepository.countBySorObject(sorObject) == 1

        when: "Storing additional partialMatches for the sorObject"
        service.storePartialMatch(sorObject, [createPersonPartialMatch("Potential #1", person1), createPersonPartialMatch("Potential #2", person2)])

        then: "there are now two partial matches"
        partialMatchRepository.countBySorObject(sorObject) == 2

        when: "assigning UID to the sorObject for person1"
        service.assignUidToSOR(sorObject, person1)

        then: "The there is no partial match records for sorObject"
        partialMatchRepository.countBySorObject(sorObject) == 0
    }

    @Unroll
    void "test recordMatchHistory"() {
        given:
        def sorObjectId = sorObject.id
        def sorId = sorObject.sor.id
        def sorPrimaryKey = sorObject.sorPrimaryKey

        when:
        def matchHistory = service.recordMatchHistory(sorObject, match, newUid)
        println JsonUtil.convertObjectToJson(matchHistory)
        def metaData = JsonUtil.convertObjectToJson(matchHistory.metaData)
        println metaData

        then:
        matchHistory.eventId == 'event123'
        matchHistory.sorObjectId == sorObjectId
        matchHistory.sorId == sorId
        matchHistory.sorPrimaryKey == sorPrimaryKey
        matchHistory.matchResultType == exptdMatchResultType
        matchHistory.actionTime
        matchHistory.uidAssigned == exptdUidAssigned
        metaData == exptdMetaData

        where:
        match                                                                                                                                                                  | newUid || exptdMatchResultType                             | exptdUidAssigned | exptdMetaData
        new PersonNoMatch(eventId: 'event123')                                                                                                                                 | "002"  || MatchHistoryResultTypeEnum.NONE_NEW_UID          | '002'            | '{}'
        new PersonNoMatch(eventId: 'event123')                                                                                                                                 | null   || MatchHistoryResultTypeEnum.NONE_NEW_UID_DEFERRED | null             | '{}'
        new PersonExactMatch(eventId: 'event123', person: new Person(uid: '001'), ruleNames: ['TEST_RULE'])                                                                    | null   || MatchHistoryResultTypeEnum.EXACT                 | '001'            | '{"exactMatch":{"ruleNames":["TEST_RULE"]}}'
        new PersonPartialMatches(eventId: 'event123', partialMatches: [new PersonPartialMatch(eventId: 'event123', person: new Person(uid: '001'), ruleNames: ['TEST_RULE'])]) | null   || MatchHistoryResultTypeEnum.POTENTIAL             | null             | '{"potentialMatchCount":1,"potentialMatches":[{"potentialMatchToUid":"001","ruleNames":["TEST_RULE"]}]}'
    }

    void "test recordMatchHistory for SORObject with uid already assigned"() {
        given:
        def sorObjectId = sorObject.id
        def sorId = sorObject.sor.id

        when:
        // should return null because sorobject already matched up and there is no new event to record
        def matchHistory = service.recordMatchHistory(sorObject, new PersonExistingMatch(eventId: 'event123', person: new Person(uid: '001')), null)

        then:
        !matchHistory
    }

    void "test recordMatchHistory for SORObject with matchOnly flag set"() {
        given:
        def sorObjectId = sorObject.id
        def sorId = sorObject.sor.id

        when:
        // should return null because matchOnly flag is true
        def matchHistory = service.recordMatchHistory(sorObject, new PersonNoMatch(eventId: 'event123', matchOnly: true), null)

        then:
        !matchHistory
    }

    private static PersonPartialMatch createPersonPartialMatch(String name, Person person) {
        return new PersonPartialMatch(eventId: 'eventId', person: person, ruleNames: [name])
    }
}
