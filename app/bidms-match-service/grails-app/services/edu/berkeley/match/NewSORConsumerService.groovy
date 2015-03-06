package edu.berkeley.match

import edu.berkeley.registry.model.SORObject
import grails.transaction.Transactional
import grails.util.Holders

import javax.jms.MapMessage

@Transactional
class NewSORConsumerService {
    // Utilizing the JMS plugin
    static exposes = ["jms"]

    static destination = Holders.config.jms.matchService.newSorObject.queue
    static isTopic = false
    static adapter = "transacted"
    static container = "transacted"

    static MATCH_FIELDS = ['systemOfRecord','sorPrimaryKey','givenName','middleName','surName','fullName','dateOfBirth','socialSecurityNumber']


    def matchClientService
    def uidClientService
    def databaseService
    def downstreamJMSService
    /**
     * Receives a message on the newSORQueue and processes it according to the rules
     * @param msg
     * @return
     */
    def onMessage(msg) {
        if (!msg instanceof MapMessage) {
            // TODO: Handle messages of wrong type. Right now expect a MapMessage, if it's not, just return null
            log.error "Received a message that was not of type MapMessage. It has been discarded: ${msg}"
            return null
        }

        def message = msg as MapMessage

        SORObject sorObject = getSorObjectFromMessage(message)

        Map sorAttributes = getAttributesFromMessage(message)

        def match = matchClientService.match(sorAttributes)
        log.debug("Response from MatchService: $match")

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

    /**
     * Finds the SORObject by systemOfRecord and sorPrimaryKey found in the MapMessage
     * @param message
     * @return a SORObject key (or null if not found)
     */
    private SORObject getSorObjectFromMessage(MapMessage message) {
        def systemOfRecord = message.getString('systemOfRecord')
        def sorPrimaryKey = message.getString('sorPrimaryKey')
        def sorObject = SORObject.getBySorAndObjectKey(systemOfRecord, sorPrimaryKey)
        log.debug("Read $systemOfRecord/$sorPrimaryKey from db: ${sorObject.sor}/${sorObject.sorPrimaryKey} (ID: ${sorObject.ident()}")
        sorObject
    }

    /**
     * Converts a mapMessage to a Map of attributes
     * @param message
     * @return
     */
    private Map getAttributesFromMessage(MapMessage message) {
        def sorAttributes = MATCH_FIELDS.collectEntries { [it, message.getString(it)] }.findAll { it.value }
        if (message.getObject('otherIds')) {
            Map otherIds = message.getObject('otherIds')
            sorAttributes.otherIds = otherIds
        }
        sorAttributes
    }
}
