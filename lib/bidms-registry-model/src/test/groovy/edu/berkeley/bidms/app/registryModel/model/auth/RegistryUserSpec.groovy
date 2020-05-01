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
package edu.berkeley.bidms.app.registryModel.model.auth

import edu.berkeley.bidms.app.registryModel.repo.auth.RegistryRoleRepository
import edu.berkeley.bidms.app.registryModel.repo.auth.RegistryUserRepository
import edu.berkeley.bidms.app.springsecurity.service.RegistryUserCredentialService
import edu.berkeley.bidms.orm.collection.RebuildableTreeSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification
import spock.lang.Unroll

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class RegistryUserSpec extends Specification {
    @Autowired
    private RegistryUserRepository registryUserRepository

    @Autowired
    private RegistryUserCredentialService registryUserCredentialService

    @Autowired
    private RegistryRoleRepository registryRoleRepository

    void "save RegistryUser"() {
        given:
        RegistryUser user = new RegistryUser("test")
        registryUserCredentialService.setPassword(user, "mypassword")
        user.roles = [] as RebuildableTreeSet<RegistryRole>
        RegistryRole role = new RegistryRole("testRole")
        user.roles << role

        when:
        registryRoleRepository.save(role)
        registryUserRepository.saveAndFlush(user)
        user = registryUserRepository.findByUsername("test")

        then:
        user?.id && user.passwordHash && user.passwordHash != "mypassword"
        !user.accountExpired && !user.accountLocked && !user.passwordExpired && user.enabled && user.active
        (user.roles*.authority).contains("testRole")
    }

    @Unroll
    void "test setPassword for http digests"() {
        given:
        RegistryUser user = new RegistryUser(startUsername)
        registryUserCredentialService.setPassword(user, startPassword)
        String startingHttpDigestHash = user.passwordHttpDigestHash

        when:
        if (secondUsername) {
            user.setUsername(secondUsername)
        }
        if (secondPassword) {
            registryUserCredentialService.setPassword(user, secondPassword)
        }

        then:
        startingHttpDigestHash == "eb1d53d8b371d14c5e0d563f01393320"
        user.passwordHttpDigestHash == exptdHash

        where:
        startUsername | startPassword | secondUsername | secondPassword || exptdHash
        "foouser"     | "foopassword" | null           | null           || "eb1d53d8b371d14c5e0d563f01393320"
        "foouser"     | "foopassword" | null           | "foopassword"  || "eb1d53d8b371d14c5e0d563f01393320"
        "foouser"     | "foopassword" | "newusername"  | "foopassword"  || "e7024ec82e41a52bc07f3ed0d4b614b3"
        "foouser"     | "foopassword" | null           | "newpassword"  || "3cf5956053bbb5077a2fa839ceebd3d5"
    }

    void "test setPassword for password hash"() {
        given:
        RegistryUser user = new RegistryUser("foouser")
        registryUserCredentialService.setPassword(user, "foopassword")
        String startingPasswordHash = user.passwordHash

        when:
        registryUserCredentialService.setPassword(user, "newpassword")

        then:
        user.passwordHash != startingPasswordHash
        user.passwordHash.startsWith('{bcrypt}$')
    }
}
