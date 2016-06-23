package edu.berkeley.match

import edu.berkeley.registry.model.*
import edu.berkeley.registry.model.types.IdentifierTypeEnum
import edu.berkeley.registry.model.types.SOREnum
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import javax.jms.MapMessage

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@SuppressWarnings("GroovyAssignabilityCheck")
// When using expectations in Spock
@TestFor(NewSORConsumerService)
@Mock([SOR, SORObject, Person, Identifier, IdentifierType])
class NewSORConsumerServiceSpec extends Specification {
    SORObject sorObject
    Person person1
    Person person2
    Person person3

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
        0 * service.databaseService.assignUidToSOR(_, _)
        0 * service.uidClientService.provisionUid(_)
        0 * service.uidClientService.provisionNewUid(_)
    }

    void "when a SOR has no match and the matchOnly flag is set as a String, then no new UID is obtained"() {
        given:
        def message = mockMessage()
        mockMatchOnlyAsString(message, true)

        when:
        def sorAttributes = [systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', matchOnly: true, otherIds: [employeeId: '123']]
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', matchOnly: true, otherIds: [employeeId: '123']]) >> new PersonNoMatch(matchOnly: true)
        0 * service.uidClientService.provisionNewUid(sorObject)
        0 * service.databaseService.assignUidToSOR(_, _)
        0 * service.uidClientService.provisionUid(_)
        0 * service.uidClientService.provisionNewUid(_)
    }

    void "when a SOR has no match and the matchOnly flag is set as a Boolean, then no new UID is obtained"() {
        given:
        def message = mockMessage()
        mockMatchOnlyAsBoolean(message, true)

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', givenName: 'givenName', surName: 'surName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN', matchOnly: true, otherIds: [employeeId: '123']]) >> new PersonNoMatch(matchOnly: true)
        0 * service.uidClientService.provisionNewUid(sorObject)
        0 * service.databaseService.assignUidToSOR(_, _)
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

    void "when a SOR has an existing match, provisioning is notified"() {
        given:
        def message = mockMessageExisting()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([systemOfRecord: 'SIS_STUDENT', sorPrimaryKey: 'SIS00002']) >> new PersonExistingMatch(person: person3)
        0 * service.databaseService.assignUidToSOR(sorObject, person1)
        0 * service.uidClientService.provisionUid(person3)
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

    private MapMessage mockMessageExisting() {
        def message = Mock(MapMessage)
        message.getString("systemOfRecord") >> SOREnum.SIS_STUDENT.name()
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
        def sor = new SOR(name: 'SIS').save(failOnError: true)
        sorObject = new SORObject(sor: sor, sorPrimaryKey: 'SIS00001').save(validate: false)
        person1 = new Person(uid: '1').save(validate: false)
        person2 = new Person(uid: '2').save(validate: false)

        IdentifierType sisStudentIdType = new IdentifierType(idName: IdentifierTypeEnum.sisStudentId.name()).save(validate: false)
        person3 = new Person(uid: '3')
        person3.addToIdentifiers(new Identifier(
                identifierType: sisStudentIdType,
                identifier: "SIS00002",
                isActive: true,
                isPrimary: true,
                person: person3
        ))
        person3.save(validate: false)

        def sisStudentSor = new SOR(name: SOREnum.SIS_STUDENT.name()).save(failOnError: true)
        new SORObject(sor: sisStudentSor, sorPrimaryKey: 'SIS00002', uid: person3.uid).save(validate: false)
    }
}
