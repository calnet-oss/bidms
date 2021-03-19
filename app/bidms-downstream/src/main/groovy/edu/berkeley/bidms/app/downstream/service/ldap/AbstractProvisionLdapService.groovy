/*
 * Copyright (c) 2017, Regents of the University of California and
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
package edu.berkeley.bidms.app.downstream.service.ldap

import edu.berkeley.bidms.app.downstream.config.properties.DownstreamConfigProperties
import edu.berkeley.bidms.app.downstream.service.DownstreamProvisionService
import edu.berkeley.bidms.app.jmsclient.service.DownstreamProvisioningJmsClientService
import edu.berkeley.bidms.app.registryModel.model.DownstreamObject
import edu.berkeley.bidms.app.registryModel.model.DownstreamSystem
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.type.DownstreamSystemEnum
import edu.berkeley.bidms.app.registryModel.repo.DownstreamObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.DownstreamSystemRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.TrackStatusRepository
import edu.berkeley.bidms.connector.ldap.LdapConnector
import edu.berkeley.bidms.connector.ldap.LdapRequestContext
import edu.berkeley.bidms.connector.ldap.UidObjectDefinition
import edu.berkeley.bidms.downstream.ldap.SystemUidObjectDefinition
import edu.berkeley.bidms.downstream.service.DownstreamProvisioningService
import edu.berkeley.bidms.downstream.service.DownstreamSystemNotFoundException
import edu.berkeley.bidms.downstream.service.ProvisioningResult
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jms.core.JmsTemplate
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import javax.annotation.PostConstruct
import javax.naming.Name
import javax.sql.DataSource
import java.sql.Timestamp

@Slf4j
//@Service("provisionLdapService")
abstract class AbstractProvisionLdapService implements DownstreamProvisioningService {

    @Autowired
    DownstreamConfigProperties downstreamConfig

    @Autowired
    DownstreamProvisionService provisionService

    @Autowired
    DataSource dataSource

    @Autowired
    DownstreamProvisioningJmsClientService downstreamProvisioningJmsClientService

    JmsTemplate downstreamJmsTemplate // TODO

    @Autowired
    DownstreamSystemRepository downstreamSystemRepository

    @Autowired
    DownstreamObjectRepository downstreamObjectRepository

    @Autowired
    TrackStatusRepository trackStatusRepository

    @Autowired
    PersonRepository personRepository

    @Qualifier("ldapConnector")
    @Autowired
    private LdapConnector ldapConnector

    @Qualifier("mainEntryUidObjectDefinition")
    @Autowired
    private UidObjectDefinition mainEntryUidObjectDefinition

    // fields for tracking timing statistics (asynchronous only)
    private static final Object lock = new Object()
    private static final Map<String, Long> counterMap = [changed: 0, unchanged: 0]
    private static final Map<String, Long> totalProcessingTimeMap = [changed: 0, unchanged: 0]
    private static final Map<String, Long> startBatchTimeMap = [changed: 0, unchanged: 0]
    private static final Map<String, Long> batchCountMap = [changed: 0, unchanged: 0]
    private static final Map<String, Long> totalBatchTimeSecondsMap = [changed: 0, unchanged: 0]
    private static final int batchSize = 1000

    UidObjectDefinition getUidObjectDefinition() {
        return mainEntryUidObjectDefinition
    }

    LdapConnector getConnector() {
        return ldapConnector
    }

    /**
     * Register this LDAP provisioning service with the primary provisioning
     * service.
     */
    @PostConstruct
    void register() {
        provisionService.register(this)
    }

    @Override
    List<String> accepts() {
        return [DownstreamSystemEnum.LDAP.name]
    }

    // we want one-tx-per-uid, not one tx-per-batch
    @Transactional(propagation = Propagation.NEVER)
    @Override
    ProvisioningResult provisionBulk(String eventId, String downstreamSystemName, boolean isSynchronous) {
        int sendQueueCount = 0
        int unchangedCount = 0
        Sql sql = new Sql(dataSource)
        try {
            DownstreamSystem downstreamSystem = null
            downstreamSystem = downstreamSystemRepository.findByName(downstreamSystemName?.toUpperCase())
            if (!downstreamSystem) {
                throw new DownstreamSystemNotFoundException(downstreamSystemName?.toUpperCase())
            }

            // to provision
            int expectedToProvisionCount = sql.firstRow(bulkToProvisionCountSql, [downstreamSystem.id]).count
            log.info("Expecting to provision $expectedToProvisionCount entries to $downstreamSystemName ${isSynchronous ? 'synchronously' : 'asynchronously'} for eventId $eventId")
            sql.eachRow(bulkToProvisionSql, [downstreamSystem.id]) { row ->
                if (asyncPersistUid(eventId, downstreamSystem, row.uid, row.globUniqId, (isSynchronous ? (Map<String, Object>) new JsonSlurper().parseText(row.objJson as String) : null), (isSynchronous ? row.hash : null), false, isSynchronous)) {
                    sendQueueCount++
                } else {
                    unchangedCount++
                }
            }

            // to delete
            int expectedToDeleteCount = sql.firstRow(bulkToDeleteCountSql, [downstreamSystem.id]).count
            if (expectedToDeleteCount) {
                log.info("Expecting to delete $expectedToDeleteCount entries from $downstreamSystemName ${isSynchronous ? 'synchronously' : 'asynchronously'} for eventId $eventId")
                sql.eachRow(bulkToDeleteSql, [downstreamSystem.id]) { row ->
                    // even though DeletedDownstreamObject has the full
                    // objJson, for deletes, the only thing that is needed
                    // in the map is uid
                    if (asyncPersistUid(eventId, downstreamSystem, row.uid, null, [uid: row.uid], null, true, isSynchronous)) {
                        sendQueueCount++
                    } else {
                        unchangedCount++
                    }
                }
            }
        }
        finally {
            sql.close()
        }

        if (isSynchronous) {
            log.info("Provisioned $sendQueueCount entries to $downstreamSystemName synchronously for eventId $eventId")
        } else {
            log.info("Sent $sendQueueCount entries to the provisioning queue for $downstreamSystemName asynchronously for eventId $eventId")
        }

        return new ProvisioningResult(downstreamSystemName: downstreamSystemName, count: sendQueueCount, unchangedCount: unchangedCount, synchronous: isSynchronous)
    }

    @Transactional(rollbackFor = Exception)
    @Override
    ProvisioningResult provisionUid(String eventId, String downstreamSystemName, String uid, boolean forceAsynchronous, boolean skipIfUnchanged) {
        Sql sql = new Sql(dataSource)
        try {
            DownstreamSystem downstreamSystem = downstreamSystemRepository.findByName(downstreamSystemName.toUpperCase())
            if (!downstreamSystem) {
                throw new DownstreamSystemNotFoundException(downstreamSystemName?.toUpperCase())
            }
            boolean isSynchronous = !forceAsynchronous
            def row = sql.firstRow(uidToProvisionSql, [downstreamSystem.id, uid])
            if (row) {
                if (!skipIfUnchanged || row.hash != row.provisionedHash || row.forceProvision) {
                    boolean wasModifiedOrSent = asyncPersistUid(eventId, downstreamSystem, uid, row.globUniqId, (isSynchronous ? (Map<String, Object>) new JsonSlurper().parseText(row.objJson as String) : null), (isSynchronous ? row.hash : null), false, isSynchronous)
                    if (isSynchronous) {
                        // if synchronous then it was an explicit request,
                        // i.e., from a REST call
                        log.trace("Provisioned uid $uid to $downstreamSystemName synchronously for eventId $eventId")
                    } else {
                        // if asynchronous then we're processing off a queue
                        // in bulk
                        log.trace("Sent uid $uid to $downstreamSystemName local queue asynchronously for eventId $eventId")
                    }
                    return new ProvisioningResult(downstreamSystemName: downstreamSystemName, count: (wasModifiedOrSent ? 1 : 0), unchangedCount: (!wasModifiedOrSent ? 1 : 0), synchronous: isSynchronous)
                } else {
                    // skipIfUnchanged is true and hash == provisionedHash,
                    // so no change is indicated and we should skip
                    return new ProvisioningResult(downstreamSystemName: downstreamSystemName, count: 0, unchangedCount: 1, synchronous: isSynchronous)
                }
            } else {
                // The uid may be a pending delete or an already-deleted or
                // unowned (ownershipLevel <= 0)
                row = sql.firstRow(uidToDeleteSql, [downstreamSystem.id, uid])
                if (row && !row.timeDeletedDownstream) {
                    // not deleted downstream yet
                    boolean wasModifiedOrSent = asyncPersistUid(eventId, downstreamSystem, uid, null, [uid: uid], null, true, isSynchronous)
                    if (isSynchronous) {
                        // if synchronous then it was an explicit request,
                        // i.e., from a REST call
                        log.info("Deleted uid $uid from $downstreamSystemName synchronously for eventId $eventId")
                    } else {
                        // if asynchronous then we're processing off a queue
                        // in bulk
                        log.debug("Sent an expected delete for uid $uid to $downstreamSystemName local queue asynchronously for eventId $eventId")
                    }
                    return new ProvisioningResult(downstreamSystemName: downstreamSystemName, count: (wasModifiedOrSent ? 1 : 0), unchangedCount: (!wasModifiedOrSent ? 1 : 0), synchronous: isSynchronous)
                } else if (row) {
                    // already deleted
                    log.info("Request was received to reprovision already-deleted uid $uid to $downstreamSystemName.  Ignoring.")
                    return new ProvisioningResult(downstreamSystemName: downstreamSystemName, count: 0, unchangedCount: 1, synchronous: isSynchronous)
                } else {
                    // uid is not found OR ownershipLevel is <= 0 in either
                    // DownstreamObject or DeletedDownstreamObject
                    log.debug("Request was received to reprovision uid $uid to $downstreamSystemName but either the uid doesn't exist or it is unowned, meaning it has an ownershipLevel of <= 0 in either DownstreamObject or DeletedDownstreamObject.")
                    return new ProvisioningResult(downstreamSystemName: downstreamSystemName, count: 0, unchangedCount: 1, synchronous: isSynchronous)
                }
            }
        }
        finally {
            sql.close()
        }
    }

    protected ProvisionLdapServiceCallbackContext createProvisionLdapServiceCallbackContext(int downstreamSystemId) {
        return new ProvisionLdapServiceCallbackContext(
                systemTypeName: ((SystemUidObjectDefinition) uidObjectDefinition).systemType,
                downstreamSystemId: downstreamSystemId
        )
    }

    protected LdapRequestContext createLdapRequestContext(String eventId, ProvisionLdapServiceCallbackContext context) {
        return new LdapRequestContext(new LdapTemplate(connector.contextSource), eventId, uidObjectDefinition, context)
    }

    protected LdapRequestContext createLdapRequestContext(String eventId, int downstreamSystemId) {
        return createLdapRequestContext(eventId, createProvisionLdapServiceCallbackContext(downstreamSystemId))
    }

    protected LdapRequestContext createLdapRequestContext(String eventId, ProvisionLdapServiceCallbackContext context, LdapContextSource ldapContextSource) {
        return new LdapRequestContext(new LdapTemplate(ldapContextSource), eventId, uidObjectDefinition, context)
    }

    /**
     * @return true if a LDAP modification actually occurred
     */
    @Transactional(rollbackFor = Exception)
    boolean persistUid(String eventId, int downstreamSystemId, String uid, String globUniqId, Map<String, Object> jsonObject, Long hash, boolean isDelete) {
        Sql sql = new Sql(dataSource)
        try {
            ProvisionLdapServiceCallbackContext context = createProvisionLdapServiceCallbackContext(downstreamSystemId)
            if (!isDelete) {
                // uid isn't part of the DownstreamObject.objJson, so add it
                jsonObject.uid = uid
                // same for the globally unique identifier
                if (globUniqId) {
                    jsonObject[uidObjectDefinition.globallyUniqueIdentifierAttributeName] = globUniqId
                }
                boolean wasModified = false
                synchronized (context.lock) {
                    wasModified = connector.persist(eventId, uidObjectDefinition, context, jsonObject, isDelete)
                    updateDownstreamObject(sql, downstreamSystemId, uid, hash)
                }
                return wasModified
            } else {
                boolean wasModified = false
                synchronized (context.lock) {
                    wasModified = connector.persist(eventId, uidObjectDefinition, context, jsonObject, isDelete)
                    markDeletedDownstreamObjectAsDeletedDownstream(sql, downstreamSystemId, uid)
                }
                log.info("uid $uid has been deleted from ${downstreamSystemRepository.get(downstreamSystemId).name}")
                return wasModified
            }
        }
        finally {
            sql.close()
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    String getBulkToProvisionSql() {
        return "SELECT uid, hash, objJson, globUniqId FROM DownstreamObjectToProvisionView WHERE systemId = ?"
    }

    String getBulkToProvisionCountSql() {
        return "SELECT count(*) AS count FROM (${bulkToProvisionSql}) sub"
    }

    String getUidToProvisionSql() {
        return "SELECT uid AS uid, hash, provisionedHash, objJson, forceProvision, globUniqId FROM DownstreamObject WHERE systemId = ? AND uid = ? AND ownershipLevel > 0"
    }

    String getUidToDeleteSql() {
        return "SELECT uid, timeDeletedDownstream FROM DeletedDownstreamObject WHERE systemId = ? AND ownershipLevel > 0 AND uid = ?"
    }

    String getBulkToDeleteSql() {
        return "SELECT uid FROM DeletedDownstreamObject WHERE systemId = ? AND timeDeletedDownstream IS NULL AND ownershipLevel > 0"
    }

    String getBulkToDeleteCountSql() {
        return "SELECT count(*) AS count FROM (${bulkToDeleteSql}) sub"
    }

    @Transactional(rollbackFor = Exception)
    @SuppressWarnings("GrMethodMayBeStatic")
    void updateDownstreamObject(Sql sql, int downstreamSystemId, String uid, long hash) {
        if (sql.executeUpdate("UPDATE DownstreamObject SET provisionedHash = ?, forceProvision = ? WHERE systemId = ? AND sysObjKey = ?" as String, [hash, false, downstreamSystemId, uid]) != 1) {
            log.warn("Couldn't find DownstreamObject for uid $uid, downstreamSystemId=$downstreamSystemId while provisioning.  Was it just deleted?")
        }
    }

    @Transactional(rollbackFor = Exception)
    void markDeletedDownstreamObjectAsDeletedDownstream(Sql sql, int downstreamSystemId, String uid) {
        if (sql.executeUpdate("UPDATE DeletedDownstreamObject SET timeDeletedDownstream = ? WHERE systemId = ? AND sysObjKey = ?" as String, [new Timestamp(new Date().time), downstreamSystemId, uid]) != 1) {
            log.warn("Couldn't find DeletedDownstreamObject for uid $uid, downstreamSystemId=$downstreamSystemId while processing deletes.  Was it just deleted?")
        }
    }

    /**
     * @return If running synchronously and true, that means LDAP was
     *         modified.  If running asynchronously and true, then a message
     *         was sent to the local provisioning queue.  Should always
     *         return true if running asynchronously.
     */
    boolean asyncPersistUid(String eventId, DownstreamSystem downstreamSystem, String uid, String globUniqId, Map<String, Object> jsonObject, Long hash, boolean isDelete, boolean forceSynchronous) {
        if (!(downstreamSystem.name in accepts())) {
            throw new RuntimeException("this service doesn't accept provisioning objects from ${downstreamSystem.name}")
        }
        boolean isSynchronous = forceSynchronous
        if (isSynchronous) {
            if (!jsonObject) {
                throw new RuntimeException("jsonObject can't be null when persisting synchronously")
            }
            if (!isDelete && hash == null) {
                throw new RuntimeException("hash can't be null when persisting a non-delete synchronously")
            }
            return persistUid(eventId, downstreamSystem.id, uid, globUniqId, jsonObject, hash, isDelete)
        } else {
            Map<String, Object> headers = [
                    eventId   : eventId,
                    fromSource: "local"
            ]
            downstreamProvisioningJmsClientService.provisionUid(downstreamJmsTemplate, downstreamSystem.name, uid, headers)
            return true
        }
    }

    String getSearchBase() {
        return downstreamConfig.ldap.searchBase
    }

    String getDirectoryUrl() {
        return downstreamConfig.ldap.url
    }

    abstract String getDcBase()

    /**
     * Provides a LdapContextSource for a particular bind DN and credentials
     * (used to bind as the user during password changes).
     *
     * @param dn Bind DN
     * @param passwd Bind credentials
     * @return A LdapContextSource object using the bind DN and bind
     *         credentials.
     */
    protected LdapContextSource getUserLdapContextSource(String dn, String passwd) {
        LdapContextSource authContextSource = new LdapContextSource()
        authContextSource.with {
            userDn = dn
            password = passwd
            url = directoryUrl
        }
        authContextSource.afterPropertiesSet()
        return authContextSource
    }

    protected LdapConnector.MatchingEntryResult findDirectoryEntryByDownstreamObject(LdapRequestContext reqCtx, DownstreamObject downstreamObject) {
        if (!(downstreamObject.system.name in accepts())) {
            throw new RuntimeException("this service doesn't work with downstream system ${downstreamObject.system.name}")
        }

        if (downstreamObject) {
            LdapConnector.MatchingEntryResult matchingEntry = connector.findMatchingEntry(reqCtx, null, downstreamObject.uid, downstreamObject.globUniqId)
            return matchingEntry
        }

        return null
    }

    /**
     * Find a person's DN in the directory based on their uid or the
     * globUniqId value in DownstreamObject.
     *
     * @param eventId The eventId
     * @param downstreamObject DownstreamObject belonging to person for
     *        which we wish to find the DN of.
     * @return The DN of the person owning the downstreamObject or null if
     *         directory entry not found for person.
     */
    protected Name getDnByDownstreamObject(String eventId, DownstreamObject downstreamObject) {
        if (!(downstreamObject.system.name in accepts())) {
            throw new RuntimeException("this service doesn't work with downstream system ${downstreamObject.system.name}")
        }

        if (downstreamObject) {
            LdapRequestContext reqCtx = createLdapRequestContext(eventId, downstreamObject.system.id)
            LdapConnector.MatchingEntryResult matchingEntry = findDirectoryEntryByDownstreamObject(reqCtx, downstreamObject)
            return matchingEntry?.entry?.dn
        }

        return null
    }

    /**
     * Find a person's DN in the directory based on their uid or the
     * globUniqId value in DownstreamObject.
     *
     * @param eventId The eventId
     * @param downstreamSystemName Name of the downstream system
     * @param person The person to find the directory DN of.
     * @return The DN of the person or null if directory entry not found for
     *         person.
     */
    String getDnByPerson(String eventId, String downstreamSystemName, Person person) {
        DownstreamSystem downstreamSystem = downstreamSystemRepository.findByName(downstreamSystemName.toUpperCase())
        if (!downstreamSystem) {
            throw new DownstreamSystemNotFoundException(downstreamSystemName?.toUpperCase())
        }

        if (!(downstreamSystem.name in accepts())) {
            throw new RuntimeException("this service doesn't work with downstream system ${downstreamSystem.name}")
        }

        DownstreamObject downstreamObject = person.downstreamObjects.find { it.system.name == downstreamSystemName.toUpperCase() && it.ownershipLevel > 0 }
        return downstreamObject ? getDnByDownstreamObject(eventId, downstreamObject)?.toString() : null
    }
}
