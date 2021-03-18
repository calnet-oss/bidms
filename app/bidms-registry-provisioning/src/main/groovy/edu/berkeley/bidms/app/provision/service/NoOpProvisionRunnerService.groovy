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
import edu.berkeley.bidms.provision.common.ProvisionRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Service

// If you wish to override this bean, create your own with @Service("provisionRunnerService")
@ConditionalOnMissingBean(name = "provisionRunnerService")
@Service("edu.berkeley.bidms.app.provision.service.NoOpProvisionRunnerService")
class NoOpProvisionRunnerService implements ProvisionRunner {

    /**
     * This is called to rebuild a person when SORObject data changes for
     * the person.  A collection of all SORObjects is in the sorPerson map
     * (i.e., a JSON form of the SORObjects).
     *
     * Implementors must take this SORObject JSON and act on it to update
     * the person.  For example, perhaps a telephone number has changed in
     * the SORObject JSON and you would then update the person.telephones
     * collection based on the new telephone number.
     *
     * There is no need to persist (save) the person as this is done by the
     * service that invokes this ProvisionRunner.
     *
     * @param person The person to rebuild.
     * @param sorPerson The aggregate SORObjects JSON as a map.
     * @return A map indicating the result: use [result: "success"] if the operation was a success.
     */
    @Override
    Map<String, ?> run(Person person, Map sorPerson) {
        // This implementation is a no-op: the person is left as-is.
        return [result: "success"]
    }
}
