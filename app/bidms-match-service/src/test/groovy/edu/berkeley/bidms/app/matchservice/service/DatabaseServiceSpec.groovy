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
import edu.berkeley.bidms.app.matchservice.service.DatabaseService
import edu.berkeley.bidms.app.registryModel.model.PartialMatch
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.SOR
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.repo.PartialMatchRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class DatabaseServiceSpec extends Specification {

    @Autowired
    SORRepository sorRepository
    @Autowired
    SORObjectRepository sorObjectRepository
    @Autowired
    PersonRepository personRepository
    @Autowired
    PartialMatchRepository partialMatchRepository

    DatabaseService service
    SORObject sorObject
    PartialMatch existingPartialMatch
    Person person1
    Person person2

    def setup() {
        this.service = new DatabaseService(sorObjectRepository, partialMatchRepository)

        SOR sor = sorRepository.save(new SOR(name: 'SIS'))
        this.sorObject = sorObjectRepository.save(new SORObject(
                sor: sor,
                sorPrimaryKey: 'SIS123',
                queryTime: new Date(),
                objJson: "{}",
                jsonVersion: 1
        ))
        this.person1 = personRepository.save(new Person(uid: "1"))
        this.person2 = personRepository.save(new Person(uid: "2"))

        def epm = new PartialMatch(person1)
        epm.sorObject = sorObject
        this.existingPartialMatch = partialMatchRepository.save(epm)
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
        def sorObject2 = sorObjectRepository.save(new SORObject(
                sor: sorRepository.save(new SOR(name: 'HR')),
                sorPrimaryKey: 'HR123',
                queryTime: new Date(),
                objJson: "{}",
                jsonVersion: 1
        ))
        def person3 = personRepository.save(new Person(uid: '3'))

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

    private static PersonPartialMatch createPersonPartialMatch(String name, Person person) {
        return new PersonPartialMatch(person, [name])
    }
}
