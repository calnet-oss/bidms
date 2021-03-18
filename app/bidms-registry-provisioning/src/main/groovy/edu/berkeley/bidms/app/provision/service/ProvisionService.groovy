/*
 * Copyright (c) 2021, Regents of the University of California and
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
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@CompileStatic
@Slf4j
// If you wish to override this bean, create your own with @Service("provisionService")
@ConditionalOnMissingBean(name = "provisionService")
@Service("edu.berkeley.bidms.app.provision.service.ProvisionService")
class ProvisionService extends AbstractProvisionService<AbstractProvisionService.ProvisionResult> {

    ProvisionService(PlatformTransactionManager transactionManager) {
        super(transactionManager)
    }

    @Override
    protected ProvisionResult newProvisionResult() {
        return new ProvisionResult()
    }

    /**
     * Called in the same transaction after person has been rebuilt and
     * saved.  Avoid asynchronous operations on the same uid here because
     * the uid rows are still locked by the transaction.
     */
    @Override
    protected void afterPersonRebuiltAndSavedInProvisionTransaction(Person person, ProvisionResult provisionResult) {
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
    @Transactional(propagation = Propagation.NEVER)
    @Override
    protected void afterPersonRebuiltAndSavedPostTransactionPreDownstream(String uid, ProvisionResult provisionResult) {
    }
}
