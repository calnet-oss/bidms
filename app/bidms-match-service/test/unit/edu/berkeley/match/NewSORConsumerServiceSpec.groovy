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
        service.downstreamJMSService = Mock(DownstreamJMSService)
    }

    void "when a SOR has no match, a new UID is retrieved from the UIDService, the SOR is updated and provisioning is notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([systemOfRecord: 'SIS', sorObjectKey: 'SIS00001', firstName: 'firstName', lastName: 'lastName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN']) >> new PersonNoMatch()
        1 * service.uidClientService.createUidForPerson(['systemOfRecord': 'SIS', 'sorObjectKey': 'SIS00001', 'firstName': 'firstName', 'lastName': 'lastName', 'dateOfBirth': 'DOB', 'socialSecurityNumber': 'SSN']) >> person1
        1 * service.databaseService.assignUidToSOR(sorObject, person1)
        1 * service.downstreamJMSService.provision(person1)
    }


    void "when a SOR has exact one match, the uid matching is assigned to the SOR and provisioning is notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([systemOfRecord: 'SIS', sorObjectKey: 'SIS00001', firstName: 'firstName', lastName: 'lastName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN']) >> new PersonExactMatch(person: person1)
        1 * service.databaseService.assignUidToSOR(sorObject, person1)
        1 * service.downstreamJMSService.provision(person1)
        0 * service.uidClientService.createUidForPerson(_)
    }

    void "when a SOR has partial matches, the matches are stored in the match bucket and provisioning is not notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([systemOfRecord: 'SIS', sorObjectKey: 'SIS00001', firstName: 'firstName', lastName: 'lastName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN']) >> new PersonPartialMatches(people: [person1, person2])
        1 * service.databaseService.storePartialMatch(sorObject, [person1, person2])
        0 * service.uidClientService.createUidForPerson(_)
        0 * service.databaseService.assignUidToSOR(*_)
        0 * service.downstreamJMSService.provision(_)
    }

    private MapMessage mockMessage() {
        def message = Mock(MapMessage)
        message.getString('systemOfRecord') >> 'SIS'
        message.getString('sorObjectKey') >> 'SIS00001'
        message.getString('firstName') >> 'firstName'
        message.getString('lastName') >> 'lastName'
        message.getString('dateOfBirth') >> 'DOB'
        message.getString('socialSecurityNumber') >> 'SSN'
        return message
    }

    private void setupModel() {
        def sor = new SOR(name: 'SIS').save(failOnError: true)
        sorObject = new SORObject(sor: sor, sorObjectKey: 'SIS00001').save(validate: false)
        person1 = new Person(uid: '1').save(validate: false)
        person2 = new Person(uid: '2').save(validate: false)

    }

}
