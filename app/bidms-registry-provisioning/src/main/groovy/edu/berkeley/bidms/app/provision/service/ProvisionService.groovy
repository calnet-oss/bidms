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
package edu.berkeley.bidms.app.provision.service

import edu.berkeley.bidms.app.jmsclient.service.ProvisioningJmsClientService
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.PersonSorObjectsJson
import edu.berkeley.bidms.app.registryModel.model.PersonSorObjectsSyncKey
import edu.berkeley.bidms.app.registryModel.repo.NameTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonSorObjectsJsonRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonSorObjectsSyncKeyRepository
import edu.berkeley.bidms.common.json.JsonUtil
import edu.berkeley.bidms.orm.transaction.JpaTransactionTemplate
import edu.berkeley.bidms.provision.common.ProvisionRunner
import edu.berkeley.bidms.provision.jms.ProvisionJmsTemplate
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager
import javax.persistence.LockModeType
import javax.sql.DataSource

@CompileStatic
@Slf4j
// If you wish to override this bean, create your own with @Service("provisionService")
@ConditionalOnMissingBean(name = "provisionService")
@Service("edu.berkeley.bidms.app.provision.service.ProvisionService")
class ProvisionService {

    @InheritConstructors
    static class ProvisionServiceException extends Exception {
    }

    @InheritConstructors
    static class EndpointException extends ProvisionServiceException {
    }

    @InheritConstructors
    static class NullResponseEndpointException extends EndpointException {
    }

    @Autowired
    EntityManager entityManager

    @Autowired
    DataSource dataSource

    @Autowired
    ProvisionRunner provisionRunnerService

    @Autowired
    AbstractDownstreamProvisioningService downstreamProvisioningService

    @Autowired
    ProvisionJmsTemplate provisionJmsTemplate

    @Autowired
    ProvisioningJmsClientService provisioningJmsClientService

    @Autowired
    PersonRepository personRepository

    @Autowired
    PersonSorObjectsSyncKeyRepository personSorObjectsSyncKeyRepository

    @Autowired
    PersonSorObjectsJsonRepository personSorObjectsJsonRepository

    @Autowired
    NameTypeRepository nameTypeRepository

    PlatformTransactionManager transactionManager
    JpaTransactionTemplate requiresNewTransactionTemplate
    private String toProvisionTableName = "PersonSorObjectsToProvisionView"

    // If you want to limit the maximum uids that can be processed in a
    // request.  Mostly useful for debugging.  Null or 0 means unlimited.
    private static final Integer maxOverallProvisionCountPerRequest = null

    // Maximum amount of errors that can occur per request before processing
    // is halted.
    private static final Integer maximumErrorsPerRequest = 100

    ProvisionService(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager
        this.requiresNewTransactionTemplate = new JpaTransactionTemplate(transactionManager, TransactionDefinition.PROPAGATION_REQUIRES_NEW)
    }

    /**
     * If returns true, provisioning will stop for the request.
     *
     * @param count Total number of uids processed (including failures)
     * @param failureCount Number of uids that failed to provision.
     * @return true if provisioning processing should halt for this request.
     */
    protected static boolean isReadyToHalt(Integer count, Integer failureCount) {
        return (maxOverallProvisionCountPerRequest && count >= maxOverallProvisionCountPerRequest) || (maximumErrorsPerRequest && failureCount >= maximumErrorsPerRequest)
    }

    /**
     * Provision all uids from the PersonSorObjectsToProvisionView.
     *
     * @return Map JSON object with response message.
     */
    @Transactional(propagation = Propagation.NEVER)
    Map bulkProvision(Boolean synchronousDownstream, String eventId) {
        log.debug("PROFILE: bulkProvision(): ENTER")
        int count = 0
        int failureCount = 0
        List<String> failedUids = []
        try {
            // provision all uids from the registry toProvision service
            String lastUid = null
            List<String> uidsToProvision
            def regSql = new Sql(dataSource)
            try {
                while ((uidsToProvision = getToProvisionUids(regSql, lastUid)) && uidsToProvision.size() > 0) {
                    for (String uid in uidsToProvision) {
                        log.debug("PROFILE: PROVISION JSON PERSON: ENTER $count")
                        try {
                            lastUid = uid
                            try {
                                toProvisionUid(uid, synchronousDownstream, eventId)
                            }
                            catch (Exception e) {
                                log.error("Couldn't provision uid=$uid.  Message=${e.message}", e)
                                failureCount++
                                failedUids.add(uid)
                            }
                            count++
                            if (count % 1000 == 0) {
                                log.debug("Processed $count users on this request")
                            }
                        }
                        finally {
                            log.debug("PROFILE: PROVISION JSON PERSON: EXIT")
                            if (isReadyToHalt(count, failureCount)) break
                        }
                    }
                    if (isReadyToHalt(count, failureCount)) break
                }
            }
            finally {
                regSql.close()
            }
            def jsonResponse = [
                    message              : "Processed $count uids with $failureCount failures",
                    totalCount           : count,
                    successCount         : count - failureCount,
                    failureCount         : failureCount,
                    synchronousDownstream: synchronousDownstream
            ] as Map<String, ?>
            if (failedUids.size() > 0) {
                jsonResponse.failedUids = failedUids
            }
            return jsonResponse
        }
        finally {
            log.debug("PROFILE: bulkProvision(): EXIT")
        }
    }

    /**
     * Provision one uid.
     *
     * @param uid The uid to provision or reprovision.
     * @param synchronousDownstream If true, then this means this service
     *        will wait for provisioning to downstream to complete before
     *        returning a result.  If false or null, then this service will
     *        notify the downstream provisioner to reprovision, but will not
     *        wait for a result.
     * @param eventId Audit event id.
     * @return Map JSON object with response message.
     */
    // Important: We must manage our own transactions within this method to avoid lock contention.
    @Transactional(propagation = Propagation.NEVER)
    Map provisionUid(String uid, Boolean synchronousDownstream, String eventId) {
        /**
         * PROFILE debug statements are detected by the
         * bin/reportProfileTimes.pl script.  If you change any PROFILE
         * messages, you'll need to change them in that script too in order
         * for the script to properly report on times.
         */
        log.debug("PROFILE: provisionUid(): ENTER")
        try {
            // provision just one uid
            toProvisionUid(uid, synchronousDownstream, eventId)
            def jsonResponse = [message: "Successfully processed 1 uid", uid: uid]
            return jsonResponse
        }
        finally {
            log.debug("PROFILE: provisionUid(): EXIT")
        }
    }

    static class ProvisionResult {
        boolean stopProcessing
    }

    /**
     * Called in the same transaction after person has been rebuilt and
     * saved.  Avoid asynchronous operations on the same uid here because
     * the uid rows are still locked by the transaction.
     */
    protected void afterPersonRebuiltAndSavedInProvisionTransaction(Person person, ProvisionResult provisionResult) {
        // default is a no-op
    }

    protected ProvisionResult newProvisionResult() {
        return new ProvisionResult()
    }

    protected ProvisionResult provisionInNewTransaction(String uid, Boolean synchronousDownstream, String eventId) {
        ProvisionResult provisionResult = newProvisionResult()

        // transaction template used because @Transactional annotation
        // ignored when this method called elsewhere from within this class
        requiresNewTransactionTemplate.execute {
            log.debug("PROFILE: provision() PersonSorObjectsJson.get() START")
            PersonSorObjectsJson psoj = personSorObjectsJsonRepository.get(uid)
            log.debug("PROFILE: provision() PersonSorObjectsJson.get() END")
            if (!psoj) {
                Person p = personRepository.get(uid)
                if (!p) {
                    log.warn("UID $uid does not appear to exist.  Consuming provisionUid message with no action.")
                    provisionResult.stopProcessing = true
                    return provisionResult
                } else {
                    throw new ProvisionServiceException("Couldn't get PersonSorObjectsJson for uid $uid but Person does exist with that uid.  Race condition?  Problem with trigger?")
                }
            }

            log.debug("PROFILE: provision() Person.get() START")
            Person person = personRepository.get(uid) // retrieve Person with a row level lock
            log.debug("PROFILE: provision() Person.get() END")
            if (!person) {
                throw new ProvisionServiceException("Cannot find Person with uid ${uid}")
            }
            // Pessimistically lock the person uid.
            requiresNewTransactionTemplate.currentEntityManager.lock(person, LockModeType.PESSIMISTIC_WRITE)
            Map sorPerson = JsonUtil.convertJsonToMap(psoj.aggregateJson)
            if (sorPerson == null) { // "{}" is considered empty, thus the explicit null check
                throw new ProvisionServiceException("Couldn't get aggregate JSON for uid $uid")
            }
            log.debug("PROFILE: provision() rebuild() START ${person.uid}")
            Map resultMap = null
            try {
                resultMap = rebuild(person, sorPerson)
            }
            finally {
                log.debug("PROFILE: provision() rebuild() END")
            }
            log.debug("PROFILE: provision() save() START ${person.uid}")
            try {
                personRepository.saveAndFlush(person)
            }
            finally {
                log.debug("PROFILE: provision() save() END")
            }

            // If UidChangeExecutor reassigned a different uid to a
            // SORObject, the UID with the newly assigned SORObject needs to
            // be marked for reprovisioning since it gained a SORObject.
            resultMap.uidsNeedingReprovision?.each { String uidWithAddedSorObject ->
                //log.debug("UID $uidWithAddedSorObject has gained a SORObject, so sending UID to provisionUid queue")
                sendToProvisionUidQueue(uidWithAddedSorObject)
            }

            afterPersonRebuiltAndSavedInProvisionTransaction(person, provisionResult)

            // store the hash of what we just provisioned in
            // PersonSorObjectsSyncKey
            log.debug("PROFILE: provision() syncKey.save() START")
            try {
                if (!psoj.jsonHash) {
                    throw new RuntimeException("PersonSorObjectsJson.jsonHash cannot be null for uid ${psoj.id}")
                }
                PersonSorObjectsSyncKey syncKey = personSorObjectsSyncKeyRepository.get(person.uid)
                if (!syncKey) {
                    syncKey = new PersonSorObjectsSyncKey()
                    syncKey.id = person.uid
                } else {
                    requiresNewTransactionTemplate.currentEntityManager.lock(syncKey, LockModeType.PESSIMISTIC_WRITE)
                }
                syncKey.provisionedJsonHash = psoj.jsonHash
                syncKey.forceProvision = false
                // There's a trigger that updates timeUpdated, but Hibernate
                // won't persist unless something has changed.  Since
                // there's no guarantee the hash has changed since last
                // provision, we still want to force Hibernate to at least
                // update the table with a new timestamp.
                syncKey.timeUpdated = new Date()
                personSorObjectsSyncKeyRepository.saveAndFlush(syncKey)
            }
            finally {
                log.debug("PROFILE: provision() syncKey.save() END")
            }

            // Clearing the cache is important here, otherwise severe
            // performance problems have been observed due to Hibernate
            // cache growth.  Without this, I observed every
            // .save() getting increasingly slower.
            flushAndClearHibernateSession(requiresNewTransactionTemplate.currentEntityManager)
        }

        return provisionResult
    }

    /**
     * Called after person has been rebuilt and saved and the transaction
     * has been fully committed.  Called before an asynchronous downstream
     * message has been sent or before synchronous downstream provisioning
     * occurs.
     *
     * This is where additional asynchronous operations on the uid may
     * happen since the locks on the uid rows should have been released.
     */
    protected void afterPersonRebuiltAndSavedPostTransactionPreDownstream(String uid, ProvisionResult provisionResult) {
        // default is a no-op
    }

    /**
     * Provision a person based on their aggregate JSON from the
     * PersonSorObjectsJson table (which is materialized from
     * PersonSorObjectsJsonView).
     *
     * The aggregate JSON is a collection of all the SORObject JSON objects
     * for a uid.
     *
     * @param uid uid that needs to be provisioned.
     * @param synchronousDownstream If true, then this means this service
     *        will wait for provisioning to downstream to complete before
     *        returning a result.  If false or null, then this service will
     *        notify the downstream provisioner to reprovision, but will not
     *        wait for a result.
     * @param eventId Audit event id.
     */
    protected void provision(String uid, Boolean synchronousDownstream, String eventId) {
        log.debug("PROFILE: provision(): ENTER")
        ProvisionResult provisionResult = provisionInNewTransaction(uid, synchronousDownstream, eventId)
        try {
            if (provisionResult.stopProcessing) {
                // some kind of caught error happened above where we just
                // want to finish consuming the message and return
                return
            }

            afterPersonRebuiltAndSavedPostTransactionPreDownstream(uid, provisionResult)

            // Request downstream provisioning when synchronously
            // provisioning a uid all the way through
            if (synchronousDownstream) {
                downstreamProvisioningService.provisionUidSynchronously(eventId, uid)
            } else {
                // Notify the downstream provisioning service of an update
                // by placing a message in its queue.
                downstreamProvisioningService.provisionUidAsynchronously(eventId, uid)
            }
        }
        finally {
            log.debug("PROFILE: provision(): EXIT")
        }
    }

    /**
     * Run the provisioning scripts to kick-off rebuilding this person based
     * on the aggregate JSON.
     *
     * @param person The person to rebuild.
     * @param sorPerson The aggregate JSON to rebuild the person with.
     */
    protected Map<String, ?> rebuild(Person person, Map sorPerson) {
        Map<String, ?> resultMap
        try {
            log.debug("PROFILE: rebuild() scriptRunner runScript: START")
            resultMap = provisionRunnerService.run(person, sorPerson)
            log.debug("PROFILE: rebuild() scriptRunner runScript: END")
            if (log.isDebugEnabled()) {
                log.debug("result: $resultMap")
            }
        }
        catch (Exception e) {
            final String msg = "Provision script threw an exception"
            log.error(msg, e)
            throw new ProvisionServiceException(msg, e)
        }
        return resultMap
    }

    protected void toProvisionUid(String uid, Boolean synchronousDownstream, String eventId) {
        try {
            log.debug("PROFILE: PROVISION JSON PERSON: START call provision")
            provision(uid, synchronousDownstream, eventId)
            log.debug("PROFILE: PROVISION JSON PERSON: END call provision")
        } catch (Exception e) {
            log.error("provision failed for uid $uid", e)
            throw e
        }
    }

    private static final Integer limitRowQuantity = 1000

    String getToProvisionTableName() {
        return toProvisionTableName
    }

    void setToProvisionTableName(String toProvisionTableName) {
        this.toProvisionTableName = toProvisionTableName
    }

    protected String getToProvisionSql(boolean greaterThanUid) {
        return "SELECT uid FROM ${getToProvisionTableName()} " + (greaterThanUid ? "WHERE uid > ?" : "") + " ORDER BY uid LIMIT $limitRowQuantity"
    }

    /**
     * Return list of UIDs needing provisioning.
     *
     * @param greaterThanUid Start the query for uids past this uid.  Used for paging.
     * @return List of uids.
     */
    private List<String> getToProvisionUids(Sql regSql, String greaterThanUid) {
        List<String> uids = []
        String sql = getToProvisionSql(greaterThanUid != null)
        List<Object> sqlParams = (greaterThanUid ? [greaterThanUid] as List<Object> : [])
        regSql.eachRow(sql, sqlParams) { row ->
            uids.add(row.getString("uid"))
        }
        return uids
    }

    @Transactional(propagation = Propagation.NEVER)
    int requeueUidsNeedingReprovision() {
        int count = 0
        log.info("Adding all UIDs to provisionUidBulk queue that need to be reprovisioned.")
        String sql = "SELECT uid FROM ${getToProvisionTableName()} ORDER BY uid"
        def regSql = new Sql(dataSource)
        try {
            regSql.eachRow(sql) { row ->
                sendToBulkProvisionUidQueue(row.getString("uid"))
                count++
            }
        }
        finally {
            regSql.close()
        }
        log.info("Added $count UIDs to provisionUid queue")
        return count
    }

    protected void sendToBulkProvisionUidQueue(String uid) {
        provisioningJmsClientService.provisionUidBulk(provisionJmsTemplate, uid)
    }

    protected void sendToProvisionUidQueue(String uid) {
        provisioningJmsClientService.provisionUid(provisionJmsTemplate, uid)
    }

    protected void flushAndClearHibernateSession(EntityManager entityManager) {
        entityManager.flush()
        entityManager.clear()
    }
}
