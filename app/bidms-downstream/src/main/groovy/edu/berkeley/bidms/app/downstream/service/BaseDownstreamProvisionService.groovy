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
package edu.berkeley.bidms.app.downstream.service

import edu.berkeley.bidms.app.common.config.properties.provisionContext.ProvisioningContextProperties
import edu.berkeley.bidms.app.jmsclient.service.DownstreamProvisioningJmsClientService
import edu.berkeley.bidms.app.registryModel.model.DownstreamSystem
import edu.berkeley.bidms.app.registryModel.repo.DownstreamSystemRepository
import edu.berkeley.bidms.downstream.jms.DownstreamProvisionJmsTemplate
import edu.berkeley.bidms.downstream.service.DownstreamProvisioningService
import edu.berkeley.bidms.downstream.service.DownstreamSystemNotFoundException
import edu.berkeley.bidms.downstream.service.ProvisioningResult
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource
import java.sql.Timestamp

@CompileStatic
@Slf4j
abstract class BaseDownstreamProvisionService<PC extends ProvisioningContextProperties> implements DownstreamProvisioningService<PC> {

    DataSource dataSource
    DownstreamSystemRepository downstreamSystemRepository
    DownstreamProvisionJmsTemplate downstreamJmsTemplate
    DownstreamProvisioningJmsClientService downstreamProvisioningJmsClientService

    BaseDownstreamProvisionService(
            DataSource dataSource,
            DownstreamSystemRepository downstreamSystemRepository,
            DownstreamProvisionJmsTemplate downstreamJmsTemplate,
            DownstreamProvisioningJmsClientService downstreamProvisioningJmsClientService
    ) {
        this.dataSource = dataSource
        this.downstreamSystemRepository = downstreamSystemRepository
        this.downstreamJmsTemplate = downstreamJmsTemplate
        this.downstreamProvisioningJmsClientService = downstreamProvisioningJmsClientService
    }

    abstract List<String> accepts()

    abstract boolean persistUid(String eventId, int downstreamSystemId, String uid, String globUniqId, Map<String, Object> jsonObject, Long hash, boolean isDelete)

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
            int expectedToProvisionCount = sql.firstRow(bulkToProvisionCountSql, [downstreamSystem.id] as List<Object>).count as Integer
            log.info("Expecting to provision $expectedToProvisionCount entries to $downstreamSystemName ${isSynchronous ? 'synchronously' : 'asynchronously'} for eventId $eventId")
            sql.eachRow(bulkToProvisionSql, [downstreamSystem.id] as List<Object>) { row ->
                if (
                        asyncPersistUid(
                                eventId,
                                downstreamSystem,
                                row.getString("uid"),
                                row.getString("globUniqId"),
                                (isSynchronous ? (Map<String, Object>) new JsonSlurper().parseText(row.getObject("objJson").toString()) : null),
                                (isSynchronous ? row.getLong("hash") : null),
                                false,
                                isSynchronous)
                ) {
                    sendQueueCount++
                } else {
                    unchangedCount++
                }
            }

            // to delete
            int expectedToDeleteCount = sql.firstRow(bulkToDeleteCountSql, [downstreamSystem.id] as List<Object>).count as Integer
            if (expectedToDeleteCount) {
                log.info("Expecting to delete $expectedToDeleteCount entries from $downstreamSystemName ${isSynchronous ? 'synchronously' : 'asynchronously'} for eventId $eventId")
                sql.eachRow(bulkToDeleteSql, [downstreamSystem.id] as List<Object>) { row ->
                    // even though DeletedDownstreamObject has the full
                    // objJson, for deletes, the only thing that is needed
                    // is the relevant attributes to search for the object(s)
                    // to delete
                    if (
                            asyncPersistUid(
                                    eventId,
                                    downstreamSystem,
                                    row.getString("uid"),
                                    null,
                                    deleteMap(row.getString("uid"), row.getString("sysObjKey"), row.getObject("objJson").toString()),
                                    null,
                                    true,
                                    isSynchronous
                            )
                    ) {
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
            def row = sql.firstRow(uidToProvisionSql, [downstreamSystem.id, uid] as List<Object>)
            if (row) {
                if (!skipIfUnchanged || row.hash != row.provisionedHash || row.forceProvision) {
                    boolean wasModifiedOrSent = asyncPersistUid(
                            eventId,
                            downstreamSystem,
                            uid,
                            row.globUniqId as String,
                            (isSynchronous ? (Map<String, Object>) new JsonSlurper().parseText(row.objJson as String) : null),
                            (isSynchronous ? row.hash as Long : null),
                            false,
                            isSynchronous
                    )
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
                row = sql.firstRow(uidToDeleteSql, [downstreamSystem.id, uid] as List<Object>)
                if (row && !row.timeDeletedDownstream) {
                    // not deleted downstream yet
                    boolean wasModifiedOrSent = asyncPersistUid(
                            eventId,
                            downstreamSystem, uid,
                            null,
                            deleteMap(uid, row.sysObjKey as String, row.objJson.toString()),
                            null,
                            true,
                            isSynchronous
                    )
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

    @SuppressWarnings("GrMethodMayBeStatic")
    String getBulkToProvisionSql() {
        return "SELECT uid, hash, objJson, globUniqId FROM DownstreamObjectToProvisionView WHERE systemId = ?"
    }

    String getBulkToProvisionCountSql() {
        return "SELECT count(*) AS count FROM (${bulkToProvisionSql}) sub"
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    String getUidToProvisionSql() {
        return "SELECT uid AS uid, hash, provisionedHash, objJson, forceProvision, globUniqId FROM DownstreamObject WHERE systemId = ? AND uid = ? AND ownershipLevel > 0"
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    String getUidToDeleteSql() {
        return "SELECT uid, sysObjKey, objJson, timeDeletedDownstream FROM DeletedDownstreamObject WHERE systemId = ? AND ownershipLevel > 0 AND uid = ?"
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    String getBulkToDeleteSql() {
        return "SELECT uid, sysObjKey, objJson FROM DeletedDownstreamObject WHERE systemId = ? AND timeDeletedDownstream IS NULL AND ownershipLevel > 0"
    }

    String getBulkToDeleteCountSql() {
        return "SELECT count(*) AS count FROM (${bulkToDeleteSql}) sub"
    }

    @Transactional(rollbackFor = Exception)
    @SuppressWarnings("GrMethodMayBeStatic")
    void updateDownstreamObject(Sql sql, int downstreamSystemId, String uid, long hash) {
        if (sql.executeUpdate("UPDATE DownstreamObject SET provisionedHash = ?, forceProvision = ? WHERE systemId = ? AND sysObjKey = ?" as String, [hash, false, downstreamSystemId, uid] as List<Object>) != 1) {
            log.warn("Couldn't find DownstreamObject for uid $uid, downstreamSystemId=$downstreamSystemId while provisioning.  Was it just deleted?")
        }
    }

    @Transactional(rollbackFor = Exception)
    @SuppressWarnings("GrMethodMayBeStatic")
    void markDeletedDownstreamObjectAsDeletedDownstream(Sql sql, int downstreamSystemId, String uid) {
        if (sql.executeUpdate("UPDATE DeletedDownstreamObject SET timeDeletedDownstream = ? WHERE systemId = ? AND sysObjKey = ?" as String, [new Timestamp(new Date().time), downstreamSystemId, uid] as List<Object>) != 1) {
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
            ] as Map<String, Object>
            downstreamProvisioningJmsClientService.provisionUid(downstreamJmsTemplate, downstreamSystem.name, uid, headers)
            return true
        }
    }

    private Map<String, Object> deleteMap(String uid, String sysObjKey, String objJson) {
        return getDeleteMap(uid, sysObjKey, objJson)
    }

    /**
     * When deleting a downstream object, this method returns a map of
     * relevant values to search the downstream system for the object to
     * delete.  Often, this is just a simple primary key.  In LDAP/AD, for
     * example, this method would typically return a map with just the uid
     * in it: e.g., {@code [uid: "UIDHERE"]}.
     *
     * @param uid uid of the person whose DownstreamObject is being deleted
     * @param sysObjKey The sysObjKey column from the DeletedDownstreamObject table
     * @param objJson The objJson column from the DeletedDownstreamObject table
     *
     * @return A map of relevant values to search for in the downstream system.
     */
    protected abstract Map<String, Object> getDeleteMap(String uid, String sysObjKey, String objJson)
}
