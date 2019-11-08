/*
 * Copyright (c) 2019, Regents of the University of California and
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
import edu.berkeley.bidms.app.registryModel.repo.TelephoneRepository
import edu.berkeley.bidms.app.registryModel.repo.TelephoneTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class TelephoneSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    TelephoneTypeRepository telephoneTypeRepository

    @Autowired
    TelephoneRepository telephoneRepository

    Class<?> getDomainClass() { return Telephone }

    void "confirm Telephone is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static Telephone[] getTestTelephones(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, TelephoneTypeRepository telephoneTypeRepository) {
        TelephoneType telephoneType = telephoneTypeRepository.findByTelephoneTypeName("testName")
        return [
                new Telephone(
                        person: TestUtil.findPerson(personRepository, "1"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "LDAP_PEOPLE", "uid123"),
                        telephoneType: telephoneType,
                        phoneNumber: 'sarcher@berkeley.edu'
                ),
                new Telephone(
                        person: TestUtil.findPerson(personRepository, "2"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "LDAP_GUEST", "uid123"),
                        telephoneType: telephoneType,
                        phoneNumber: 'sarcher@berkeley.edu'
                ),
                new Telephone(
                        person: TestUtil.findPerson(personRepository, "3"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "LDAP_ADVCON", "uid123"),
                        telephoneType: telephoneType,
                        phoneNumber: 'sarcher@berkeley.edu'
                )
        ]
    }

    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
        SORObjectSpec.insertSorObjects(personRepository, sorRepository, sorObjectRepository)
    }

    static synchronized void insertTelephones(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, TelephoneTypeRepository telephoneTypeRepository, TelephoneRepository telephoneRepository) {
        telephoneTypeRepository.save(new TelephoneType(telephoneTypeName: "testName"))
        getTestTelephones(personRepository, sorRepository, sorObjectRepository, telephoneTypeRepository).each {
            telephoneRepository.saveAndFlush(it)
        }
    }

    void "save test"() {
        when:
        insertTelephones(personRepository, sorRepository, sorObjectRepository, telephoneTypeRepository, telephoneRepository)
        List<Telephone> expected = getTestTelephones(personRepository, sorRepository, sorObjectRepository, telephoneTypeRepository)
        List<Telephone> actual = telephoneRepository.findAll() as List<Telephone>

        then:
        expected == actual
    }
}
