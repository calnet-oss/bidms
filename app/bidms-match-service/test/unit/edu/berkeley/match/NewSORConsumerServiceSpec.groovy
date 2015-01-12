package edu.berkeley.match

import grails.test.mixin.TestFor
import spock.lang.Specification

import javax.jms.MapMessage

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(NewSORConsumerService)
class NewSORConsumerServiceSpec extends Specification {

    def setup() {
        service.matchClientService = Mock(MatchClientService)
        service.uidClientService = Mock(UidClientService)
    }

    void "when a SOR has no match, a new UID is retrieved from the UIDService, the SOR is updated and provisioning is notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([givenName: 'firstName', familyName: 'lastName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN']) >> new NoMatchResponse()
        1 * service.uidClientService.nextUid >> '12345'
        1 * service.databaseService.assignUidToSOR('SIS00001', '12345')
        1 * service.downstreamJMSService.provision('12345')
    }


    void "when a SOR has exact one match, the uid matching is assigned to the SOR and provisioning is notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([givenName: 'firstName', familyName: 'lastName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN']) >> new ExactMatchResponse(uid: '12345')
        0 * service.uidClientService.nextUid
        1 * service.databaseService.assignUidToSOR('SIS00001', '12345')
        1 * service.downstreamJMSService.provision('12345')
    }

    void "when a SOR has partial matches, the matches are stored in the match bucket and provisioning is not notified"() {
        given:
        def message = mockMessage()

        when:
        service.onMessage(message)

        then:
        1 * service.matchClientService.match([givenName: 'firstName', familyName: 'lastName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN']) >> new PartielMatchResponse(uids: ['12345', '23456'])
        0 * service.uidClientService.nextUid
        1 * service.databaseService.storePartialMatch('SIS', 'SIS0001', ['12345', '23456'])
        0 * service.databaseService.assignUidToSOR('SIS00001', '12345')
        0 * service.downstreamJMSService.provision('12345')
    }

    private void mockMessage() {
        def message = Mock(MapMessage)
        message.getString('systemOfRecord') >> 'SIS'
        message.getString('identifier') >> 'SIS00001'
        message.getString('givenName') >> 'firstName'
        message.getString('familyName') >> 'lastName'
        message.getString('dateOfBirth') >> 'DOB'
        message.getString('socialSecurityNumber') >> 'SSN'
    }
}
