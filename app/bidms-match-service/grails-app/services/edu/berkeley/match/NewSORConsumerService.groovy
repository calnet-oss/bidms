package edu.berkeley.match

import edu.berkeley.registry.model.SORObject
import grails.transaction.Transactional
import org.apache.camel.Handler
import org.apache.camel.Message
import org.apache.camel.component.jms.JmsMessage
import org.hibernate.ObjectNotFoundException

import javax.jms.MapMessage

@Transactional
class NewSORConsumerService {

    static MATCH_FIELDS = ['systemOfRecord','sorPrimaryKey','givenName','middleName','surName','fullName','dateOfBirth','socialSecurityNumber']

    def matchClientService
    def uidClientService
    def databaseService

    @Handler
    public Object onMessage(Message camelMsg) {
        return onMessage(camelMsg.getBody())
    }

    public Object onMessage(JmsMessage camelJmsMsg) {
        return onMessage(camelJmsMsg.getJmsMessage())
    }
    /**
     * Receives a message on the newSORQueue and processes it according to the rules
     * @param msg
     * @return
     */
    public Object onMessage(javax.jms.Message msg) {
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
        // if it is an exact match assign the uid and provision
        if(match instanceof PersonExactMatch) {
            databaseService.assignUidToSOR(sorObject, match.person)
            uidClientService.provisionUid(match.person)
            return null
        }
        // provision a new person
        uidClientService.provisionNewUid(sorObject)
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
        if(!sorObject) {
            log.error("SORObject sorName=$systemOfRecord, sorPrimaryKey=$sorPrimaryKey could not be found in the DB while processing message ${message.getJMSMessageID()} from the New SORObject Queue")
            throw new ObjectNotFoundException("$systemOfRecord/$sorPrimaryKey", "SORObject")
        }
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
