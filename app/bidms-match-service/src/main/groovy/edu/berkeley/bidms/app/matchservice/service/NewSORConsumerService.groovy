/*
 * Copyright (c) 2015, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.bidms.app.matchservice.service

import edu.berkeley.bidms.app.matchservice.PersonExactMatch
import edu.berkeley.bidms.app.matchservice.PersonExistingMatch
import edu.berkeley.bidms.app.matchservice.PersonMatch
import edu.berkeley.bidms.app.matchservice.PersonNoMatch
import edu.berkeley.bidms.app.matchservice.PersonPartialMatches
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.hibernate.ObjectNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import javax.jms.MapMessage
import javax.jms.Message
import javax.persistence.EntityManager

@Slf4j
@Service
class NewSORConsumerService {

    // these correspond to properties in SorKeyData from the
    // registry-sor-key-data plugin
    static MATCH_STRING_FIELDS = ['systemOfRecord', 'sorPrimaryKey', 'givenName', 'middleName', 'surName', 'fullName', 'dateOfBirth', 'socialSecurityNumber']
    static MATCH_BOOLEAN_FIELDS = ['matchOnly']

    @Autowired
    MatchClientService matchClientService

    @Autowired
    UidClientService uidClientService

    @Autowired
    DatabaseService databaseService

    @Autowired
    EntityManager entityManager

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    NewSORConsumerService() {
    }

    NewSORConsumerService(MatchClientService matchClientService, UidClientService uidClientService, DatabaseService databaseService, EntityManager entityManager, SORRepository sorRepository, SORObjectRepository sorObjectRepository) {
        this.matchClientService = matchClientService
        this.uidClientService = uidClientService
        this.databaseService = databaseService
        this.entityManager = entityManager
        this.sorRepository = sorRepository
        this.sorObjectRepository = sorObjectRepository
    }

    /**
     * Receives a message on the newSORObjectQueue and processes it according to
     * the rules
     */
    @Transactional(propagation = Propagation.NEVER)
    @JmsListener(destination = '${bidms.matchservice.jms.new-sorobject.queue-name}')
    void onMessage(Message msg) {
        handleMessage(msg)
    }

    @PackageScope
    @Transactional(propagation = Propagation.NEVER)
    Map<String, String> handleMessage(Message msg) {
        try {
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
        catch (Exception e) {
            log.error("onMessage() failed", e)
            throw e
        }
        finally {
            // avoid hibernate cache growth
            try {
                entityManager?.clear()
            }
            catch (Exception e) {
                log.error("failed to clear hibernate session at the end of onMessage()", e)
            }
        }
    }

    @Transactional(propagation = Propagation.NEVER)
    Map<String, String> matchPerson(SORObject sorObject, Map<String, Object> sorAttributes, boolean synchronousDownstream = true) {
        // done in a new transaction
        PersonMatch personMatch = doMatch(sorObject, sorAttributes)
        // resumes previous read-only transaction
        String newlyGeneratedUid = doProvisionIfNecessary(personMatch, sorObject, synchronousDownstream)

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
            log.debug("Attempting to match $sorAttributes for SORObject(sor=${sorObject.sor.name}, sorObjKey=${sorObject.sorPrimaryKey})")
            if (log.debugEnabled) {
                // Redact SSN and DOB from log
                Map<String, Object> displaySorAttributes = [:]
                displaySorAttributes.putAll(sorAttributes)
                if (displaySorAttributes.containsKey("socialSecurityNumber")) {
                    displaySorAttributes.socialSecurityNumber = "*****"
                }
                if (displaySorAttributes.containsKey("dateOfBirth")) {
                    displaySorAttributes.dateOfBirth = "****-**-**"
                }
                log.debug("Attempting to match $displaySorAttributes for SORObject(sor=${sorObject.sor.name}, sorObjKey=${sorObject.sorPrimaryKey})")
            }
            PersonMatch match = matchClientService.match(sorAttributes)
            log.info("Response from MatchService for SORObject(sor=${sorObject.sor.name}, sorObjKey=${sorObject.sorPrimaryKey}): $match")

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
                entityManager?.clear()
            }
            catch (Exception e) {
                log.error("failed to clear hibernate session at the end of doMatch()", e)
            }
        }
    }

    /**
     * @return If a new uid was generated for the SORObject, the uid is returned, otherwise null is returned.
     */
    @Transactional(propagation = Propagation.NEVER)
    String doProvisionIfNecessary(PersonMatch match, SORObject sorObject, boolean synchronousDownstream) {
        // if it is an exact match, provision
        if (match instanceof PersonExactMatch) {
            uidClientService.provisionUid(match.person, synchronousDownstream)
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
                return uidClientService.provisionNewUid(sorObject, synchronousDownstream)
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
        def sorObject = sorObjectRepository.findBySorAndSorPrimaryKey(sorRepository.findByName(systemOfRecord), sorPrimaryKey)
        if (!sorObject) {
            log.error("SORObject sorName=$systemOfRecord, sorPrimaryKey=$sorPrimaryKey could not be found in the DB while processing message ${message.getJMSMessageID()} from the New SORObject Queue")
            throw new ObjectNotFoundException("$systemOfRecord/$sorPrimaryKey", "SORObject")
        }
        log.debug("Read $systemOfRecord/$sorPrimaryKey from db: ${sorObject.sor}/${sorObject.sorPrimaryKey} (ID: ${sorObject.id})")
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
