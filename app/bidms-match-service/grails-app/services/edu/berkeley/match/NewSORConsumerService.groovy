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

    // these correspond to properties in SorKeyData from the registry-sor-key-data plugin
    static MATCH_STRING_FIELDS = ['systemOfRecord', 'sorPrimaryKey', 'givenName', 'middleName', 'surName', 'fullName', 'dateOfBirth', 'socialSecurityNumber']
    static MATCH_BOOLEAN_FIELDS = ['matchOnly']

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
        if (!(msg instanceof MapMessage)) {
            throw new RuntimeException("Received a message that was not of type MapMessage: $msg")
        }
        MapMessage message = (MapMessage) msg

        SORObject sorObject
        try {
            sorObject = getSorObjectFromMessage(message)
        }
        catch (ObjectNotFoundException e) {
            log.error("SORObject no longer exists.  Consuming message to get it off the queue.", e)
            return null
        }

        Map<String, Object> sorAttributes = getAttributesFromMessage(message)

        matchPerson(sorObject, sorAttributes)
        return null
    }

    public void matchPerson(SORObject sorObject, Map<String, Object> sorAttributes) {
        log.debug("Attempting to match $sorAttributes")
        def match = matchClientService.match(sorAttributes)
        log.debug("Response from MatchService: $match")

        // If it is a partial match just store the partial and return
        if (match instanceof PersonPartialMatches) {
            databaseService.storePartialMatch(sorObject, match.people)
            return
        }
        // if it is an exact match assign the uid and provision
        if (match instanceof PersonExactMatch) {
            databaseService.assignUidToSOR(sorObject, match.person)
            uidClientService.provisionUid(match.person)
            return
        }
        // if it's an existing match, do nothing
        if (match instanceof PersonExistingMatch) {
            return
        }
        // provision a new person
        if (!(match instanceof PersonNoMatch))
            throw new RuntimeException("Expecting match to be an instanceof PersonNoMatch.  Instead it's: ${match?.getClass()?.name}")
        PersonNoMatch personNoMatch = (PersonNoMatch) match
        /**
         * If matchOnly=true, then matchOnly flag was true on match input,
         * meaning this person should not go to the newUid queue.  This
         * happens when we receive data about a person from a SOR where the
         * "SOR" really isn't the true System of Record for the person.
         * Example: Employees in Campus Solutions that were imported from
         * HCM.
         */
        if (!personNoMatch.matchOnly) {
            uidClientService.provisionNewUid(sorObject)
        }
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
        if (!sorObject) {
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
    private Map<String, Object> getAttributesFromMessage(MapMessage message) {
        def sorAttributes = MATCH_STRING_FIELDS.collectEntries { [it, message.getString(it)] }.findAll { it.value } +
                MATCH_BOOLEAN_FIELDS.collectEntries { [it, message.getString(it) as Boolean ?: message.getBoolean(it)] }.findAll { it.value }
        if (message.getObject('otherIds')) {
            Map otherIds = (Map) message.getObject('otherIds')
            sorAttributes.otherIds = otherIds
        }
        sorAttributes
    }
}
