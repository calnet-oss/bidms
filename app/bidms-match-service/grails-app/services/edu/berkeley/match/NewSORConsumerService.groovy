package edu.berkeley.match

import edu.berkeley.registry.model.SORObject
import grails.transaction.Transactional
import grails.util.Holders

import javax.jms.MapMessage

@Transactional
class NewSORConsumerService {
    // Utilizing the JMS plugin
    static exposes = ["jms"]

    static destination = Holders.config.jms.newSORQueue
    static isTopic = false
    static adapter = "transacted"
    static container = "transacted"

    static MATCH_FIELDS = ['systemOfRecord','sorIdentifier','givenName','familyName','dateOfBirth','socialSecurityNumber']


    def matchClientService
    def uidClientService
    def databaseService
    def downstreamJMSService

    def onMessage(msg) {
        if (!msg instanceof MapMessage) {
            // TODO: Handle messages of wrong type. Right now expect a MapMessage, if it's not, just return null
            log.error "Received a message that was not of type MapMessage. It has been discarded: ${msg}"
            return null
        }
        def message = msg as MapMessage
        def systemOfRecord = message.getString('systemOfRecord')
        def sorObjectKey = message.getString('sorIdentifier')
        def sorObject = SORObject.getBySorAndObjectKey(systemOfRecord,sorObjectKey)
        def sorAttributes = MATCH_FIELDS.collectEntries { [it, message.getString(it)] }
        def match = matchClientService.match(sorAttributes)

        // If it is a partial match just store the partial and return
        if(match instanceof PersonPartialMatches) {
            databaseService.storePartialMatch(sorObject, match.people)
            return null
        }

        // If it is an exact match assign get the UID from the match otherwise go and get a new UID
        def person = match instanceof PersonExactMatch ? match.person : uidClientService.createUidForPerson(sorAttributes)
        databaseService.assignUidToSOR(sorObject, person)
        downstreamJMSService.provision(person)

        return null
    }
}
