package edu.berkeley.match

import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.SOR
import edu.berkeley.registry.model.SORObject
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import javax.jms.MapMessage

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@SuppressWarnings("GroovyAssignabilityCheck")
// When using expectations in Spock
@TestFor(NewSORConsumerService)
@Mock([SOR, SORObject, Person])
class NewSORConsumerServiceSpec extends Specification {
    SORObject sorObject
    Person person1
    Person person2

    def setup() {
        setupModel()
        service.matchClientService = Mock(MatchClientService)
        service.uidClientService = Mock(UidClientService)
        service.databaseService = Mock(DatabaseService)
    }

    void "when a SOR has no match, a new UID is retrieved from the UIDService, the SOR is updated and provisioning is notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', otherIds: [employeeId: '123']]) >> new PersonNoMatch()
        1 * service.uidClientService.provisionNewUid(sorObject)
        0 * service.databaseService.assignUidToSOR(_,_)
        0 * service.uidClientService.provisionUid(_)
        0 * service.uidClientService.provisionNewUid(_)
    }


    void "when a SOR has exact one match, the uid matching is assigned to the SOR and provisioning is notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', otherIds: [employeeId: '123']]) >> new PersonExactMatch(person: person1)
        1 * service.databaseService.assignUidToSOR(sorObject, person1)
        1 * service.uidClientService.provisionUid(person1)
        0 * service.uidClientService.provisionNewUid(_)
    }

    void "when a SOR has partial matches, the matches are stored in the match bucket and provisioning is not notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', otherIds: [employeeId: '123']]) >> new PersonPartialMatches(people: [person1, person2])
        1 * service.databaseService.storePartialMatch(sorObject, [person1, person2])
        0 * service.uidClientService.provisionNewUid(_)
        0 * service.databaseService.assignUidToSOR(*_)
        0 * service.uidClientService.provisionUid(person1)
    }

    void "check that service can be called directly to match record"() {
        when:
        service.matchPerson(sorObject, [systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', otherIds: [employeeId: '123']])

        then:
        1 * service.matchClientService.match([systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', otherIds: [employeeId: '123']]) >> new PersonPartialMatches(people: [person1, person2])
        1 * service.databaseService.storePartialMatch(sorObject, [person1, person2])
        0 * service.uidClientService.provisionNewUid(_)
        0 * service.databaseService.assignUidToSOR(*_)
        0 * service.uidClientService.provisionUid(person1)
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

    private void setupModel() {
        def sor = new SOR(name: 'SIS').save(failOnError: true)
        sorObject = new SORObject(sor: sor, sorPrimaryKey: 'SIS00001').save(validate: false)
        person1 = new Person(uid: '1').save(validate: false)
        person2 = new Person(uid: '2').save(validate: false)

    }

}
