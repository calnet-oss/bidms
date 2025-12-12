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
import edu.berkeley.bidms.app.registryModel.model.Identifier
import edu.berkeley.bidms.app.registryModel.model.IdentifierType
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.SOR
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.repo.IdentifierRepository
import edu.berkeley.bidms.app.registryModel.repo.IdentifierTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import jakarta.jms.MapMessage
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class NewSORConsumerServiceSpec extends Specification {
    NewSORConsumerService service
    @Autowired
    PersonRepository personRepository
    @Autowired
    SORRepository sorRepository
    @Autowired
    SORObjectRepository sorObjectRepository
    @Autowired
    IdentifierTypeRepository identifierTypeRepository
    @Autowired
    IdentifierRepository identifierRepository

    SORObject sorObject
    Person person1
    Person person2
    Person person3
    List<PersonPartialMatch> personPartialMatches

    def setup() {
        this.service = new NewSORConsumerService(Mock(MatchClientService), Mock(UidClientService), Mock(DatabaseService), Mock(EntityManager), sorRepository, sorObjectRepository)
        setupModel()
    }

    void cleanup() {
        cleanupModel()
    }

    void "when a SOR has no match, a new UID is retrieved from the UIDService, the SOR is updated and provisioning is notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match(_, [systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', otherIds: [employeeId: '123']]) >> new PersonNoMatch()
        1 * service.uidClientService.provisionNewUid(_, sorObject, true)
        0 * service.databaseService.assignUidToSOR(_, _)
        0 * service.uidClientService.provisionUid(_, _)
        0 * service.uidClientService.provisionNewUid(_, _, _)
    }

    void "when a SOR has no match and the matchOnly flag is set as a String, then no new UID is obtained"() {
        given:
        def message = mockMessage()
        mockMatchOnlyAsString(message, true)

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match(_, [systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', matchOnly: true, otherIds: [employeeId: '123']]) >> new PersonNoMatch(matchOnly: true)
        0 * service.uidClientService.provisionNewUid(_, sorObject)
        0 * service.databaseService.assignUidToSOR(_, _)
        0 * service.uidClientService.provisionUid(_, _)
        0 * service.uidClientService.provisionNewUid(_, _, _)
    }

    void "when a SOR has no match and the matchOnly flag is set as a Boolean, then no new UID is obtained"() {
        given:
        def message = mockMessage()
        mockMatchOnlyAsBoolean(message, true)

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match(_, [systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', matchOnly: true, otherIds: [employeeId: '123']]) >> new PersonNoMatch(matchOnly: true)
        0 * service.uidClientService.provisionNewUid(_, sorObject)
        0 * service.databaseService.assignUidToSOR(_, _)
        0 * service.uidClientService.provisionUid(_, _)
        0 * service.uidClientService.provisionNewUid(_, _, _)
    }


    void "when a SOR has exact one match, the uid matching is assigned to the SOR and provisioning is notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match(_, [systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', otherIds: [employeeId: '123']]) >> new PersonExactMatch(person: person1)
        1 * service.databaseService.assignUidToSOR(sorObject, person1)
        1 * service.uidClientService.provisionUid(person1, true)
        0 * service.uidClientService.provisionNewUid(_, _, _)
    }

    void "when a SOR has an existing match, provisioning is notified"() {
        given:
        def message = mockMessageExisting()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match(_, [systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00002']) >> new PersonExistingMatch(person: person3)
        0 * service.databaseService.assignUidToSOR(sorObject, person1)
        0 * service.uidClientService.provisionUid(person3, _)
        0 * service.uidClientService.provisionNewUid(_, _, _)
    }

    void "when a SOR has partial matches, the matches are stored in the match bucket and provisioning is not notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match(_, [systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', otherIds: [employeeId: '123']]) >> new PersonPartialMatches(personPartialMatches)
        1 * service.databaseService.storePartialMatch(sorObject, personPartialMatches)
        0 * service.uidClientService.provisionNewUid(_, _, _)
        0 * service.databaseService.assignUidToSOR(*_)
        0 * service.uidClientService.provisionUid(person1, _)
    }

    void "check that service can be called directly to match record"() {
        when:
        service.matchPerson("eventId", sorObject, [systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', otherIds: [employeeId: '123']])

        then:
        1 * service.matchClientService.match(_, [systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', otherIds: [employeeId: '123']]) >> new PersonPartialMatches(personPartialMatches)
        1 * service.databaseService.storePartialMatch(sorObject, personPartialMatches)
        0 * service.uidClientService.provisionNewUid(_, _, _)
        0 * service.databaseService.assignUidToSOR(*_)
        0 * service.uidClientService.provisionUid(person1, _)
    }

    private MapMessage mockMessage() {
        def message = Mock(MapMessage)
        message.getString('systemOfRecord') >> 'SIS'
        message.getString('sorPrimaryKey') >> 'SIS00001'
        message.getString('givenName') >> 'givenName'
        message.getString('surName') >> 'surName'
        message.getString('dateOfBirth') >> 'DOB'
        message.getString('socialSecurityNumber') >> 'SSN'
        message.getObject('otherIds') >> [employeeId: '123']
        return message
    }

    private MapMessage mockMessageExisting() {
        def message = Mock(MapMessage)
        message.getString("systemOfRecord") >> "SIS"
        message.getString("sorPrimaryKey") >> 'SIS00002'
        return message
    }

    private void mockMatchOnlyAsBoolean(MapMessage message, Boolean matchOnly) {
        message.itemExists("matchOnly") >> true
        message.getBoolean("matchOnly") >> matchOnly
    }

    private void mockMatchOnlyAsString(MapMessage message, Boolean matchOnly) {
        message.itemExists("matchOnly") >> true
        message.getString("matchOnly") >> matchOnly?.toString()
    }

    private void setupModel() {
        def sor = sorRepository.saveAndFlush(new SOR(name: 'SIS'))
        this.sorObject = sorObjectRepository.saveAndFlush(new SORObject(
                sor: sor,
                sorPrimaryKey: 'SIS00001',
                objJson: '{}',
                jsonVersion: 1,
                queryTime: new Date()
        ))
        this.person1 = personRepository.saveAndFlush(new Person(uid: '1'))
        this.person2 = personRepository.saveAndFlush(new Person(uid: '2'))

        IdentifierType studentIdType = identifierTypeRepository.saveAndFlush(new IdentifierType(idName: "studentId"))
        person3 = new Person(uid: '3')
        def id = new Identifier(person3)
        id.with {
            identifierType = studentIdType
            identifier = "SIS00002"
            isActive = true
            isPrimary = true
        }
        this.person3 = personRepository.saveAndFlush(person3)

        sorObjectRepository.saveAndFlush(new SORObject(
                sor: sor,
                sorPrimaryKey: 'SIS00002',
                person: person3,
                objJson: '{}',
                jsonVersion: 1,
                queryTime: new Date()
        ))
        this.personPartialMatches = [createPersonPartialMatch("Potential #1", person1), createPersonPartialMatch("Potential #2", person2)]
    }

    private void cleanupModel() {
        identifierRepository.deleteAll()
        sorObjectRepository.deleteAll()
        personRepository.deleteAll()
        sorRepository.deleteAll()
    }

    private static createPersonPartialMatch(String name, Person person) {
        return new PersonPartialMatch(eventId: 'eventId', person: person, ruleNames: [name])
    }
}
