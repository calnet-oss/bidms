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

import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager
import javax.sql.DataSource

@Slf4j
@Service
class NewUidService {

    @Autowired
    DataSource dataSource

    @Autowired
    ProvisionService provisionService

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    PersonRepository personRepository

    @Autowired
    EntityManager entityManager

    @CompileStatic
    @InheritConstructors
    static class NewUidServiceException extends Exception {
    }

    static class NewUidResult {
        boolean hasExistingUid
        boolean uidGenerationSuccessful // note: true if hasExistingUid==true
        String uid
        boolean provisioningSuccessful
        Exception provisioningException
        String sorPrimaryKey
        String sorName
    }

    /**
     * Returns uid created.
     *
     * It is possible for the uid to be created and a new row inserted into
     * the Person table, but the "provisioning" of the SORObject data for
     * the person may fail.  This is because these two steps require
     * separate transactions.  This state is indicated when
     * NewUidResult.uidGenerationSuccessful is true, provisioningSuccessful
     * is false and provisioningException is nonnull.  If the provisioning
     * fails and the caller wishes to try again, then the provision service
     * rather than the new UID service should be called to retry
     * provisioning.
     *
     * To be clear: An exception gets thrown from this method if uid
     * generation fails.  If an exception occurs during the provisioning
     * phase, after the uid has been generated, then a NewUidResult is
     * returned with the exception in NewUidResult.provisioningException.
     *
     * @param sorObjectId The SORObject id of the originating SOR record for
     *        this new UID.  The uid column for this SORObject will be set
     *        to the new uid if UID generation is successful.
     * @param synchronousDownstream If true, then this means this service
     *        will wait for provisioning to downstream to complete before
     *        returning a result.  If false or null, then this service will
     *        notify the downstream provisioner to reprovision, but will not
     *        wait for a result.
     * @param eventId Audit event id
     * @return A NewUidResult object indicating the uid generated and if
     *         provisioning of the SORObject data for that uid was
     *         successful.  Contains the provisioning exception if the
     *         provisioning step failed.
     */
    NewUidResult provisionNewUid(Long sorObjectId, Boolean synchronousDownstream, String eventId) throws NewUidServiceException {
        if (sorObjectId == null) {
            throw new NewUidServiceException("sorObjectId cannot be null")
        }

        // We need person committed before we try to provision it because
        // provisioning happens in its own transaction.
        NewUidResult result = saveNewPerson(sorObjectId)
        entityManager.clear()

        if (result.uidGenerationSuccessful) {
            // hook that can be used to execute code before a provision
            // (used by tests)
            beforeProvision()

            // When creating a new uid we also want to run the provisioning
            // scripts since there's at least one SORObject associated with the
            // uid.
            try {
                // The provisionService will start a new transaction for
                // provisioning.
                provisionService.provisionUid(result.uid, synchronousDownstream, eventId)
                result.provisioningSuccessful = true
            }
            catch (Exception e) {
                // Since a provisioning error isn't fatal to creating a UID (it
                // had a separate transaction), we note the provisioning failure
                // in the result.  The caller decides what to do from there.
                // (One possibility is the caller calls the provision service to
                // retry, or the caller relies on the batch provisioning system
                // to retry the provisioning at a later time.)
                log.error("Provisioning upon new uid generation failed", e)
                result.provisioningSuccessful = false
                result.provisioningException = e
            }
        }

        return result
    }

    // We need person committed before we try to provision it because
    // provisioning happens in its own transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected NewUidResult saveNewPerson(long sorObjectId) throws NewUidServiceException {
        NewUidResult result = new NewUidResult()

        SORObject sorObject = sorObjectRepository.get(sorObjectId)
        if (!sorObject) {
            log.error("Couldn't find sorObjectId=$sorObjectId")
            result.uidGenerationSuccessful = false
            result.provisioningSuccessful = false
            result.provisioningException = new Exception("Couldn't find sorObjectId=$sorObjectId")
        } else if (sorObject.person) {
            // Could have been from an outside matching process.
            log.warn("sorObjectId=$sorObjectId already has a uid assigned to it: uid=${sorObject.person.uid}")
            result.hasExistingUid = true
            result.uidGenerationSuccessful = true
            result.uid = sorObject.person.uid
            result.sorPrimaryKey = sorObject.sorPrimaryKey
            result.sorName = sorObject.sor.name
        } else {
            String newUid = getNextUid()
            if (!newUid) {
                throw new NewUidServiceException("uid failed to generate")
            }
            Person person = new Person(uid: newUid)
            personRepository.saveAndFlush(person)
            sorObject.person = person
            sorObjectRepository.saveAndFlush(sorObject)
            result.uidGenerationSuccessful = true
            result.uid = person.uid
            result.sorPrimaryKey = sorObject.sorPrimaryKey
            result.sorName = sorObject.sor.name
        }

        return result
    }

    @Transactional(propagation = Propagation.MANDATORY)
    protected String getNextUid() {
        Sql regSql = new Sql(dataSource)
        def row = regSql.firstRow("select nextval('uid_seq') as uid" as String)
        return row?.uid
    }

    /**
     * Called after the new uid has been generated and committed as a
     * transaction, but before the provision transaction starts.
     */
    protected void beforeProvision() {
        // currently tests override this
    }
}
