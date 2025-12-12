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

import edu.berkeley.bidms.app.registryModel.repo.PersonPronounRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.PronounTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class PersonPronounSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    PronounTypeRepository pronounTypeRepository

    @Autowired
    PersonPronounRepository personPronounRepository

    Class<?> getDomainClass() { return PersonPronoun }

    void "confirm PersonPronoun is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static PersonPronoun[] getTestPronouns(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, PronounTypeRepository pronounTypeRepository) {
        PronounType pronounType = pronounTypeRepository.findByPronounTypeName("testName")
        return [
                new PersonPronoun(
                        person: TestUtil.findPerson(personRepository, "1"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "HR_PERSON", "hr123"),
                        pronounType: pronounType,
                        pronoun: "She"
                ),
                new PersonPronoun(
                        person: TestUtil.findPerson(personRepository, "2"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "SIS_STUDENT", "sisStudent123"),
                        pronounType: pronounType,
                        pronoun: "He"
                ),
                new PersonPronoun(
                        person: TestUtil.findPerson(personRepository, "3"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "ALUMNI", "alumni123"),
                        pronounType: pronounType,
                        pronoun: "They"
                )
        ]
    }

    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
        SORObjectSpec.insertSorObjects(personRepository, sorRepository, sorObjectRepository)
    }

    static synchronized void insertPronouns(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, PronounTypeRepository pronounTypeRepository, PersonPronounRepository personPronounRepository) {
        pronounTypeRepository.saveAndFlush(new PronounType(pronounTypeName: "testName"))
        // assign right uid to the SORObjects
        [["HR_PERSON", "hr123"], ["SIS_STUDENT", "sisStudent123"], ["ALUMNI", "alumni123"]].eachWithIndex { List<String> entry, int i ->
            SORObject sorObject = TestUtil.findSORObject(sorRepository, sorObjectRepository, entry[0], entry[1])
            sorObject.person = TestUtil.findPerson(personRepository, (i + 1).toString())
            sorObjectRepository.save(sorObject)
        }
        getTestPronouns(personRepository, sorRepository, sorObjectRepository, pronounTypeRepository).each {
            pronounTypeRepository.saveAndFlush(it)
        }
    }

    void "save test"() {
        when:
        insertPronouns(personRepository, sorRepository, sorObjectRepository, pronounTypeRepository, personPronounRepository)
        List<PersonPronoun> expected = getTestPronouns(personRepository, sorRepository, sorObjectRepository, pronounTypeRepository)
        List<PersonPronoun> actual = personPronounRepository.findAll() as List<PersonPronoun>

        then:
        expected == actual
    }

}
