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
package edu.berkeley.bidms.app.registryModel.model.credentialManagement

import edu.berkeley.bidms.app.registryModel.model.Identifier
import edu.berkeley.bidms.app.registryModel.model.IdentifierType
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.PersonSpec
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.model.SORObjectSpec
import edu.berkeley.bidms.app.registryModel.model.SORSpec
import edu.berkeley.bidms.app.registryModel.model.TestUtil
import edu.berkeley.bidms.app.registryModel.repo.IdentifierRepository
import edu.berkeley.bidms.app.registryModel.repo.IdentifierTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import edu.berkeley.bidms.app.registryModel.repo.credentialManagement.CredentialTokenRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class CredentialTokenSpec extends Specification {
    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    IdentifierTypeRepository identifierTypeRepository

    @Autowired
    IdentifierRepository identifierRepository

    @Autowired
    CredentialTokenRepository credentialTokenRepository

    Class<?> getDomainClass() { return CredentialToken }

    void "confirm CredentialToken is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static CredentialToken[] getTestCredentialTokens(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, IdentifierRepository identifierRepository) {
        return [
                new CredentialToken(
                        token: "foobar",
                        person: personRepository.get("1"),
                        expiryDate: new Date(),
                        identifier: identifierRepository.findByPersonAndSorObjectAndIdentifier(
                                personRepository.get("1"),
                                TestUtil.findSORObject(sorRepository, sorObjectRepository, "HR_PERSON", "hr123"),
                                "hr123"
                        )
                )
        ]
    }

    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
        SORObjectSpec.insertSorObjects(personRepository, sorRepository, sorObjectRepository)
    }

    static synchronized void insertCredentialTokens(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, IdentifierTypeRepository identifierTypeRepository, IdentifierRepository identifierRepository, CredentialTokenRepository credentialTokenRepository) {
        IdentifierType identifierType = identifierTypeRepository.saveAndFlush(new IdentifierType(idName: "hr"))
        SORObject sorObject = TestUtil.findSORObject(sorRepository, sorObjectRepository, "HR_PERSON", "hr123")
        Person person = personRepository.get("1")
        sorObject.person = person
        sorObjectRepository.saveAndFlush(sorObject)
        Identifier ident = new Identifier(person)
        ident.with {
            it.sorObject = sorObject
            it.identifier = "hr123"
            it.identifierType = identifierType
        }
        identifierRepository.saveAndFlush(ident)
        getTestCredentialTokens(personRepository, sorRepository, sorObjectRepository, identifierRepository).each {
            credentialTokenRepository.saveAndFlush(it)
        }
    }

    void "save test"() {
        when:
        insertCredentialTokens(personRepository, sorRepository, sorObjectRepository, identifierTypeRepository, identifierRepository, credentialTokenRepository)
        CredentialToken token = credentialTokenRepository.findByPersonAndToken(personRepository.get("1"), "foobar")

        then:
        token
        token.person.uid == "1"
        // confirm random token generated
        token.token
    }
}
