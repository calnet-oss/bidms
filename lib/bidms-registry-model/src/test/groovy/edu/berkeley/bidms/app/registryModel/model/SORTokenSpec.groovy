/*
 * Copyright (c) 2023, Regents of the University of California and
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
package edu.berkeley.bidms.app.registryModel.model

import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import edu.berkeley.bidms.app.registryModel.repo.SORTokenRepository
import edu.berkeley.bidms.app.registryModel.repo.SORTokenTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class SORTokenSpec extends Specification {

    @Autowired
    SORRepository sorRepository

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    SORTokenTypeRepository sorTokenTypeRepository

    @Autowired
    SORTokenRepository sorTokenRepository

    Class<?> getDomainClass() { return SORToken }

    void "confirm SORToken is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static SORToken[] getTestTokens(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, SORTokenTypeRepository sorTokenTypeRepository) {
        SORTokenType sorTokenType = sorTokenTypeRepository.findByTokenTypeName("testTokenType")
        return [
                new SORToken(
                        person: TestUtil.findPerson(personRepository, "1"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "LDAP_PEOPLE", "uid123"),
                        tokenType: sorTokenType,
                        token: 'abcdef',
                        expirationTime: new Date(1690236897633)
                )
        ]
    }

    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
        SORObjectSpec.insertSorObjects(personRepository, sorRepository, sorObjectRepository)
    }

    static synchronized void insertSORTokens(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, SORTokenTypeRepository sorTokenTypeRepository, SORTokenRepository sorTokenRepository) {
        sorTokenTypeRepository.saveAndFlush(new SORTokenType(tokenTypeName: "testTokenType"))
        // assign right uid to the SORObjects
        ["LDAP_PEOPLE"].eachWithIndex { String entry, int i ->
            SORObject sorObject = TestUtil.findSORObject(sorRepository, sorObjectRepository, entry, "uid123")
            sorObject.person = TestUtil.findPerson(personRepository, (i + 1).toString())
            sorObjectRepository.save(sorObject)
        }
        getTestTokens(personRepository, sorRepository, sorObjectRepository, sorTokenTypeRepository).each {
            sorTokenRepository.saveAndFlush(it)
        }
    }

    void "save test"() {
        when:
        insertSORTokens(personRepository, sorRepository, sorObjectRepository, sorTokenTypeRepository, sorTokenRepository)
        List<SORToken> expected = getTestTokens(personRepository, sorRepository, sorObjectRepository, sorTokenTypeRepository) as List<SORToken>
        List<SORToken> actual = sorTokenRepository.findAll()

        then:
        expected == actual
        actual.each {
            assert it.token == 'abcdef'
        }
    }
}
