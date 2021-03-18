/*
 * Copyright (c) 2018, Regents of the University of California and
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
package edu.berkeley.bidms.app.provision.test

import edu.berkeley.bidms.app.registryModel.model.auth.RegistryRole
import edu.berkeley.bidms.app.registryModel.model.auth.RegistryUser
import edu.berkeley.bidms.app.registryModel.repo.auth.RegistryRoleRepository
import edu.berkeley.bidms.app.registryModel.repo.auth.RegistryUserRepository
import edu.berkeley.bidms.app.springsecurity.service.RegistryUserCredentialService
import edu.berkeley.bidms.orm.collection.RebuildableTreeSet

class TestUtil {
    static void addTestUser(
            RegistryUserCredentialService registryUserCredentialService,
            RegistryUserRepository registryUserRepository,
            RegistryRoleRepository registryRoleRepository
    ) {
        // password is 'testpassword'
        RegistryUser registryUser = new RegistryUser("testuser")
        registryUser.roles = [registryRoleRepository.saveAndFlush(new RegistryRole("registryProvisioning"))] as RebuildableTreeSet<RegistryRole>
        registryUserCredentialService.setPassword(registryUser, "testpassword")
        registryUserRepository.saveAndFlush(registryUser)
    }

    static void deleteTestUser(RegistryUserRepository registryUserRepository, RegistryRoleRepository registryRoleRepository) {
        RegistryUser registryUser = registryUserRepository.findByUsername("testuser")
        registryUser.roles.each { registryRoleRepository.delete(it) }
        registryUserRepository.delete(registryUser)
        registryRoleRepository.delete(registryRoleRepository.findByAuthority("registryProvisioning"))
    }
}
