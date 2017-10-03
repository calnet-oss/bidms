package edu.berkeley.match

import edu.berkeley.registry.model.SORObject
import grails.transaction.Transactional
import org.apache.camel.Exchange
import org.apache.camel.Handler
import org.apache.camel.Message
import org.apache.camel.component.jms.JmsMessage
import org.hibernate.ObjectNotFoundException
import org.hibernate.SessionFactory
import org.springframework.transaction.annotation.Propagation

import javax.jms.MapMessage

@Transactional(rollbackFor = Exception, readOnly = true)
class NewSORConsumerService {

    // these correspond to properties in SorKeyData from the
    // registry-sor-key-data plugin
    static MATCH_STRING_FIELDS = ['systemOfRecord', 'sorPrimaryKey', 'givenName', 'middleName', 'surName', 'fullName', 'dateOfBirth', 'socialSecurityNumber']
    static MATCH_BOOLEAN_FIELDS = ['matchOnly']

    MatchClientService matchClientService
    UidClientService uidClientService
    DatabaseService databaseService
    SessionFactory sessionFactory

    @Handler
    void process(Exchange exchange) {
        Map<String,String> result = onMessage(exchange.in)
        exchange.out.body = result
    }

    Map<String,String> onMessage(Message camelMsg) {
        try {
            return onMessage(camelMsg.getBody())
        }
        catch (Exception e) {
            log.error("onMessage() failed", e)
            throw e
        }
    }

    Map<String,String> onMessage(JmsMessage camelJmsMsg) {
        try {
            return onMessage(camelJmsMsg.getJmsMessage())
        }
        finally {
            // avoid hibernate cache growth
            try {
                sessionFactory?.currentSession?.clear()
            }
            catch (Exception e) {
                log.error("failed to clear hibernate session at the end of onMessage()", e)
            }
        }
    }

    /**
     * Receives a message on the newSORQueue and processes it according to
     * the rules
     */
    Map<String,String> onMessage(javax.jms.Message msg) {
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

        return matchPerson(sorObject, getAttributesFromMessage(message))
    }

    Map<String, String> matchPerson(SORObject sorObject, Map<String, Object> sorAttributes) {
        // done in a new transaction
        PersonMatch personMatch = doMatch(sorObject, sorAttributes)
        // resumes previous read-only transaction
        String newlyGeneratedUid = doProvisionIfNecessary(personMatch, sorObject)

        Map<String, String> resultMap = [:]
        if (personMatch instanceof PersonExactMatch) {
            resultMap.matchType = "exactMatch"
            resultMap.uid = ((PersonExactMatch) personMatch).person.uid
        } else if (personMatch instanceof PersonExistingMatch) {
            resultMap.matchType = "existingMatch"
            resultMap.uid = ((PersonExistingMatch) personMatch).person.uid
        } else if (personMatch instanceof PersonNoMatch) {
            resultMap.matchType = "noMatch"
            // newlyGeneratedUid not guaranteed to be set.  It won't be set
            // if matchOnly is true or if there was an error when calling
            // the uid assignment service.
            resultMap.uid = newlyGeneratedUid
        }

        return resultMap
    }

    @Transactional(rollbackFor = Exception, propagation = Propagation.REQUIRES_NEW)
    PersonMatch doMatch(SORObject sorObject, Map<String, Object> sorAttributes) {
        try {
            log.debug("Attempting to match $sorAttributes")
            PersonMatch match = matchClientService.match(sorAttributes)
            log.debug("Response from MatchService: $match")

            // If it is a partial match just store the partial and return
            if (match instanceof PersonPartialMatches) {
                databaseService.storePartialMatch(sorObject, match.partialMatches)
            }
            // if it is an exact match assign the uid and provision
            else if (match instanceof PersonExactMatch) {
                databaseService.assignUidToSOR(sorObject, match.person)
            }

            // if it's an existing match, do nothing

            return match
        }
        finally {
            // avoid hibernate cache growth
            try {
                sessionFactory?.currentSession?.clear()
            }
            catch (Exception e) {
                log.error("failed to clear hibernate session at the end of doMatch()", e)
            }
        }
    }

    /**
     * @return If a new uid was generated for the SORObject, the uid is returned, otherwise null is returned.
     */
    String doProvisionIfNecessary(PersonMatch match, SORObject sorObject) {
        // if it is an exact match, provision
        if (match instanceof PersonExactMatch) {
            uidClientService.provisionUid(match.person)
        } else if (match instanceof PersonExistingMatch || match instanceof PersonPartialMatches) {
            // do nothing
        } else if (match instanceof PersonNoMatch) {
            // provision a new person
            PersonNoMatch personNoMatch = (PersonNoMatch) match

            /**
             * If matchOnly=true, then matchOnly flag was true on match
             * input, meaning this person should not go to the newUid queue. 
             * This happens when we receive data about a person from a SOR
             * where the "SOR" really isn't the true System of Record for
             * the person.  Example: Employees in Campus Solutions that were
             * imported from HCM.
             */
            if (!personNoMatch.matchOnly) {
                return uidClientService.provisionNewUid(sorObject)
            } else {
                log.info("sorObjectId=${sorObject.id}, sorPrimaryKey=${sorObject.sorPrimaryKey}, sorName=${sorObject.sor.name} didn't match with anyone and matchOnly is set to true.  This SORObject is not being sent to that newUid queue.  Instead, it's expected LdapSync will later sync it up to a UID provisioned by the legacy system.")
            }
        } else {
            throw new RuntimeException("Unexpected match type: ${match?.getClass()?.name}")
        }
        return null /* no new uid */
    }

    /**
     * Finds the SORObject by systemOfRecord and sorPrimaryKey found in the
     * MapMessage.
     *
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
     * Converts a mapMessage to a Map of attributes.
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
