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

import edu.berkeley.bidms.app.common.config.properties.provisionContext.ProvisioningContextConfigProperties
import edu.berkeley.bidms.app.common.config.properties.provisionContext.ProvisioningContextProperties
import edu.berkeley.bidms.app.downstream.config.properties.DownstreamConfigProperties
import edu.berkeley.bidms.app.downstream.service.BaseDownstreamProvisionService
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
import edu.berkeley.bidms.downstream.jms.DownstreamProvisionJmsTemplate
import edu.berkeley.bidms.downstream.ldap.LdapConflictResolutionAware
import edu.berkeley.bidms.downstream.ldap.SystemUidObjectDefinition
import edu.berkeley.bidms.downstream.service.DownstreamSystemNotFoundException
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.stereotype.Service

import javax.naming.Name
import javax.sql.DataSource

@Slf4j
// If you wish to override this bean, create your own with @Service("provisionLdapService")
@ConditionalOnMissingBean(name = "provisionLdapService")
@Service("edu.berkeley.bidms.app.downstream.service.ldap.ProvisionLdapService")
class ProvisionLdapService<PC extends ProvisioningContextProperties> extends BaseDownstreamProvisionService<PC> {

    @Value('${bidms.downstream.app-name}')
    String APP_NAME

    @Autowired
    ProvisioningContextConfigProperties provisioningContextConfigProperties

    @Autowired
    DownstreamConfigProperties downstreamConfig

    @Autowired
    DownstreamProvisionService provisionService

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

    ProvisionLdapService(
            DataSource dataSource,
            DownstreamSystemRepository downstreamSystemRepository,
            DownstreamProvisionJmsTemplate downstreamJmsTemplate,
            DownstreamProvisioningJmsClientService downstreamProvisioningJmsClientService
    ) {
        super(dataSource, downstreamSystemRepository, downstreamJmsTemplate, downstreamProvisioningJmsClientService)
    }

    @Override
    PC getProvisioningContext() {
        return provisioningContextConfigProperties.ldap
    }

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

    protected ProvisionLdapServiceCallbackContext createProvisionLdapServiceCallbackContext(int downstreamSystemId) {
        return new ProvisionLdapServiceCallbackContext(
                systemTypeName: ((SystemUidObjectDefinition) uidObjectDefinition).systemType,
                downstreamSystemId: downstreamSystemId
        )
    }

    /**
     * @return true if a LDAP modification actually occurred
     */
    @Override
    boolean persistUid(String eventId, int downstreamSystemId, String uid, String globUniqId, Map<String, Object> jsonObject, Long hash, boolean isDelete) {
        // This is a method that is called from non-transactional methods so we have to manage the start of the transaction manually.
        boolean wasModified = false
        Sql sql = new Sql(dataSource)
        try {
            final def log = log
            sql.withTransaction {
                ProvisionLdapServiceCallbackContext context = createProvisionLdapServiceCallbackContext(downstreamSystemId)
                if (!isDelete) {
                    // uid isn't part of the DownstreamObject.objJson, so add it
                    jsonObject.uid = uid
                    // same for the globally unique identifier
                    if (globUniqId) {
                        jsonObject[uidObjectDefinition.globallyUniqueIdentifierAttributeName] = globUniqId
                    }
                    if (uidObjectDefinition instanceof LdapConflictResolutionAware) {
                        // You may wish to make sure that there aren't other
                        // entries that will conflict with this object.
                        // I.e., the schema may be enforcing attribute
                        // uniqueness on certain attributes and you need to
                        // resolve any collisions before trying to persist
                        // this object.
                        ((LdapConflictResolutionAware) uidObjectDefinition).conflictResolvers?.each { resolver ->
                            resolver.queryAndResolve(APP_NAME, connector, (UidObjectDefinition) uidObjectDefinition, eventId, downstreamSystemId, uid, globUniqId, jsonObject)
                        }
                    }
                    synchronized (context.lock) {
                        wasModified = connector.persist(eventId, uidObjectDefinition, context, jsonObject, isDelete)
                        updateDownstreamObject(sql, downstreamSystemId, uid, hash)
                    }
                } else {
                    synchronized (context.lock) {
                        wasModified = connector.persist(eventId, uidObjectDefinition, context, jsonObject, isDelete)
                        markDeletedDownstreamObjectAsDeletedDownstream(sql, downstreamSystemId, uid)
                    }
                    log.info("uid $uid has been deleted from ${downstreamSystemRepository.get(downstreamSystemId).name}")
                }
            }
        }
        finally {
            sql.close()
        }
        return wasModified
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

    String getSearchBase() {
        return downstreamConfig.ldap.searchBase
    }

    String getDirectoryUrl() {
        return downstreamConfig.ldap.url
    }

    String getDcBase() {
        return provisioningContext.dcBase
    }

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

    /**
     * AD and LDAP typically use uid as the primary key.  This method
     * returns a map as {@code [uid: "UIDHERE"]} which is used to search the
     * directory for the uid when performing a delete operation.  The
     * attribute name in this map can be overridden with
     * {@link UidObjectDefinition#getPrimaryKeyAttributeName()}.
     *
     * @param uid UID of the entity being deleted.
     * @param sysObjKey The sysObjKey column from the
     *        DeletedDownstreamObject table (unused in default AD/LDAP
     *        implementation).
     * @param globUniqId The globUniqId column from the
     *        DeletedDownstreamObject table (unused in default AD/LDAP
     *        implementation).
     * @param objJson The objJson column from the DeletedDownstreamObject
     *        table (unused in default AD/LDAP implementation).
     *
     * @return A map with just the AD/LDAP primary key in it, typically the uid.
     */
    @Override
    protected Map<String, Object> getDeleteMap(String uid, String sysObjKey, String globUniqId, String objJson) {
        return [
                (uidObjectDefinition.primaryKeyAttributeName): uid
        ] as Map<String, Object>
    }
}
