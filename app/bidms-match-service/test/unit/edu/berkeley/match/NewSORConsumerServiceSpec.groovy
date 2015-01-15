package edu.berkeley.match

import grails.test.mixin.TestFor
import spock.lang.Specification

import javax.jms.MapMessage

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@SuppressWarnings("GroovyAssignabilityCheck") // When using expectations in Spock
@TestFor(NewSORConsumerService)
class NewSORConsumerServiceSpec extends Specification {

    def setup() {
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
        1 * service.matchClientService.match([systemOfRecord: 'SIS', sorIdentifier: 'SIS00001', givenName: 'firstName', familyName: 'lastName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN']) >> new NoMatch()
        1 * UidClientService.getNextUid >> '12345'
        1 * service.databaseService.assignUidToSOR('SIS', 'SIS00001', '12345')
        1 * service.downstreamJMSService.provision('12345')
    }


    void "when a SOR has exact one match, the uid matching is assigned to the SOR and provisioning is notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([systemOfRecord: 'SIS', sorIdentifier: 'SIS00001', givenName: 'firstName', familyName: 'lastName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN']) >> new ExactMatch(uid: '12345')
        1 * service.databaseService.assignUidToSOR('SIS', 'SIS00001', '12345')
        1 * service.downstreamJMSService.provision('12345')
        0 * service.uidClientService.getNextUid
    }

    void "when a SOR has partial matches, the matches are stored in the match bucket and provisioning is not notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([systemOfRecord: 'SIS', sorIdentifier: 'SIS00001', givenName: 'firstName', familyName: 'lastName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN']) >> new PartialMatch(uids: ['12345', '23456'])
        1 * service.databaseService.storePartialMatch('SIS', 'SIS00001', ['12345', '23456'])
        0 * service.uidClientService.getNextUid
        0 * service.databaseService.assignUidToSOR(*_)
        0 * service.downstreamJMSService.provision(_)
    }

    private MapMessage mockMessage() {
        def message = Mock(MapMessage)
        message.getString('systemOfRecord') >> 'SIS'
        message.getString('sorIdentifier') >> 'SIS00001'
        message.getString('givenName') >> 'firstName'
        message.getString('familyName') >> 'lastName'
        message.getString('dateOfBirth') >> 'DOB'
        message.getString('socialSecurityNumber') >> 'SSN'
        return message
    }
}
