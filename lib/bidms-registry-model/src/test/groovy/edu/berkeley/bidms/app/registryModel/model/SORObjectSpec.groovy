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
import edu.berkeley.bidms.common.json.JsonUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class SORObjectSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    Class<?> getDomainClass() { return SORObject }

    void "confirm SORObject is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static SORObject[] getTestSorObjects(PersonRepository personRepository, SORRepository sorRepository) {
        Date now = new Date()
        Person person = personRepository.findById("1").get()
        return [
                [sorPrimaryKey: "hr123", sor: "HR_PERSON"],
                [sorPrimaryKey: "hr124", sor: "HR_PERSON"],
                [sorPrimaryKey: "hr125", sor: "HR_PERSON"],
                [sorPrimaryKey: "sisStudent123", sor: "SIS_STUDENT"],
                [sorPrimaryKey: "sisAffiliate123", sor: "SIS_AFFILIATE"],
                [sorPrimaryKey: "sisAdmit123", sor: "SIS_ADMIT"],
                [sorPrimaryKey: "sisDelegate123", sor: "SIS_DELEGATE"],
                [sorPrimaryKey: "alumni123", sor: "ALUMNI"],
                [sorPrimaryKey: "uid123", sor: "LDAP_PEOPLE"],
                [sorPrimaryKey: "uid123", sor: "LDAP_GUEST"],
                [sorPrimaryKey: "uid123", sor: "LDAP_ADVCON"],
                [sorPrimaryKey: "uid123", sor: "LDAP_PRESIR"],
                [sorPrimaryKey: "uid123", sor: "LDAP_EXPIRED"]
        ].collect {
            SOR sor = sorRepository.findByName(it.sor)
            assert it.sor && sor
            new SORObject(sorPrimaryKey: it.sorPrimaryKey, sor: sor, queryTime: now, person: person, objJson: "{}", jsonVersion: 1)
        }
    }

    static synchronized void insertSorObjects(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository) {
        getTestSorObjects(personRepository, sorRepository).each { sorObject ->
            sorObjectRepository.saveAndFlush(sorObject)
        }
    }

    void "save test"() {
        when:
        SORSpec.insertSorNames(sorRepository)
        PersonSpec.insertPeople(personRepository)
        insertSorObjects(personRepository, sorRepository, sorObjectRepository)

        then:
        sorObjectRepository.findAll().size() > 0
        getTestSorObjects(personRepository, sorRepository).each { sorObject ->
            sorObjectRepository.findBySorAndSorPrimaryKey(sorObject.sor, sorObject.sorPrimaryKey).each {
                assert it.sorPrimaryKey == sorObject.sorPrimaryKey
                assert it.sor.id == sorObject.sor.id
            }
        }
    }

    def "test that finding a SORObject with unknown sor or key returns null"() {
        expect:
        !sorObjectRepository.findBySorAndSorPrimaryKey(sorRepository.findByName('HR'), '123')

        and:
        !sorObjectRepository.findBySorAndSorPrimaryKey(sorRepository.findByName('SIS'), '321')
    }

    def "test json"() {
        given:
        SOR testSOR = sorRepository.save(new SOR(name: "SIS"))
        def json = JsonUtil.convertMapToJson([id: 3, name: 'archer'])
        def obj = sorObjectRepository.save(new SORObject(sor: testSOR, sorPrimaryKey: '123', objJson: json))

        expect:
        obj.json.id == 3
        obj.json.name == 'archer'
    }
}
