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
        log.info("In onMessage for a Camel Message: body.class=${camelMsg.body.getClass().name}")
        return onMessage(camelMsg.getBody())
    }

    public Object onMessage(JmsMessage camelJmsMsg) {
        log.info("In onMessage for a Camel JmsMessage: jmsMessage.class=${camelJmsMsg.jmsMessage.getClass().name}")
        return onMessage(camelJmsMsg.getJmsMessage())
    }
    /**
     * Receives a message on the newSORQueue and processes it according to the rules
     * @param msg
     * @return
     */
    public Object onMessage(javax.jms.Message msg) {
        if (!(msg instanceof MapMessage)) {
            // TODO: Handle messages of wrong type. Right now expect a MapMessage, if it's not, just return null
            log.error "Received a message that was not of type MapMessage. It has been discarded: ${msg}"
            return null
        }

        MapMessage message = (MapMessage)msg

        SORObject sorObject = getSorObjectFromMessage(message)

        Map sorAttributes = getAttributesFromMessage(message)

        matchPerson(sorObject, sorAttributes)
        return null
    }

    public void matchPerson(SORObject sorObject, Map sorAttributes) {
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
        uidClientService.provisionNewUid(sorObject)
    }

    /**
     * Finds the SORObject by systemOfRecord and sorPrimaryKey found in the MapMessage
     * @param message
     * @return a SORObject key (or null if not found)
     */
    private SORObject getSorObjectFromMessage(MapMessage message) {
        log.info("message.class=${message.getClass().name}, superclass=${message.getClass().superclass.name}")
        log.info("message.class.interfaces=${message.getClass().interfaces*.name}")
        log.info("message.jmsType=${message.getJMSType()}")
        log.info("message.jmsMessageId=${message.getJMSMessageID()}")
        log.info("message.propertyNames=${message.propertyNames}")
        message.propertyNames.each {
            log.info("message property name=${it}")
        }
        log.info("isBytesMessage=${message instanceof javax.jms.BytesMessage}")
        log.info("isMapMessage=${message instanceof javax.jms.MapMessage}")
        log.info("isObjectMessage=${message instanceof javax.jms.ObjectMessage}")
        log.info("isStreamMessage=${message instanceof javax.jms.StreamMessage}")
        log.info("isTextMessage=${message instanceof javax.jms.TextMessage}")
        if(message instanceof groovy.util.Proxy) {
            log.info("Is a groovy proxy.  adaptee.class=${message.adaptee.getClass().name}")
        }
        else {
            log.info("is not a groovy proxy")
        }
        if(message instanceof MapMessage) {
            log.info("mapNames=${message.mapNames}")
        }
        /*
        if(message instanceof org.apache.activemq.command.ActiveMQMessage) {
            log.info("message.size=${message.size}")
            log.info("message.type=${message.type}")
            log.info("message.dataStructure=${message.dataStructure}")
            log.info("message.content=${message.content}, contentLength=${message.content?.length}")
            log.info("message.properties=${message.properties}")
            try {
              String content = new String(message.contnt.data, "UTF-8")
              log.info("content as string = $content")
            }
            catch(Exception e) {
              log.info("couldn't unmarshall byte content as a string: ${e.message}")
            }
            message.allPropertyNames.each {
                log.info("message property name=${it}")
            }
        }
        */
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
