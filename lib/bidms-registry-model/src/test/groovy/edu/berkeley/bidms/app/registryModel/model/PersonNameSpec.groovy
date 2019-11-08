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

import edu.berkeley.bidms.app.registryModel.repo.NameTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonNameRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class PersonNameSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    NameTypeRepository nameTypeRepository

    @Autowired
    PersonNameRepository personNameRepository

    Class<?> getDomainClass() { return PersonName }

    void "confirm PersonName is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    void "test honorificsAsMap"() {
        given:
        PersonName obj = new PersonName()
        obj.setHonorificsAsList([
                "JD",
                "PhD"
        ])
        expect:
        obj.honorifics == '["JD","PhD"]'
        obj.honorificsAsList == [
                "JD",
                "PhD"
        ]
    }

    static PersonName[] getTestNames(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, NameTypeRepository nameTypeRepository) {
        NameType nameType = nameTypeRepository.findByTypeName("testName")
        return [
                new PersonName(
                        person: TestUtil.findPerson(personRepository, "1"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "HR_PERSON", "hr123"),
                        nameType: nameType,
                        prefix: "Mr",
                        givenName: "John",
                        middleName: "M",
                        surName: "Smith"
                ),
                new PersonName(
                        person: TestUtil.findPerson(personRepository, "2"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "SIS_STUDENT", "sisStudent123"),
                        nameType: nameType,
                        prefix: "Mrs",
                        fullName: "Smithy, Jane M"
                ),
                new PersonName(
                        person: TestUtil.findPerson(personRepository, "3"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "ALUMNI", "alumni123"),
                        nameType: nameType,
                        givenName: "David",
                        surName: "Parker",
                        suffix: "Jr."
                )
        ]
    }

    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
        SORObjectSpec.insertSorObjects(personRepository, sorRepository, sorObjectRepository)
    }

    static synchronized void insertNames(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, NameTypeRepository nameTypeRepository, PersonNameRepository personNameRepository) {
        nameTypeRepository.save(new NameType(typeName: "testName"))
        // assign right uid to the SORObjects
        [["HR_PERSON", "hr123"], ["SIS_STUDENT", "sisStudent123"], ["ALUMNI", "alumni123"]].eachWithIndex { List<String> entry, int i ->
            SORObject sorObject = TestUtil.findSORObject(sorRepository, sorObjectRepository, entry[0], entry[1])
            sorObject.person = TestUtil.findPerson(personRepository, (i + 1).toString())
            sorObjectRepository.save(sorObject)
        }
        getTestNames(personRepository, sorRepository, sorObjectRepository, nameTypeRepository).each {
            personNameRepository.saveAndFlush(it)
        }
    }

    void "save test"() {
        when:
        insertNames(personRepository, sorRepository, sorObjectRepository, nameTypeRepository, personNameRepository)
        List<PersonName> expected = getTestNames(personRepository, sorRepository, sorObjectRepository, nameTypeRepository)
        List<PersonName> actual = personNameRepository.findAll() as List<PersonName>

        then:
        expected == actual
    }

    void "honorifics test"() {
        when:
        nameTypeRepository.save(new NameType(typeName: "testHonor"))
        Person person1 = personRepository.get("1")
        PersonName name = new PersonName(
                nameType: nameTypeRepository.findByTypeName("testHonor"),
                sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "HR_PERSON", "hr123"),
                prefix: "Mr",
                givenName: "JohnyHonor",
                middleName: "M",
                surName: "SmithyHonor",
                suffix: "Sr."
        )
        name.honorificsAsList = ["PhD"]
        person1.addToNames(name)

        personRepository.saveAndFlush(person1)
        name = person1.names?.find {
            it.nameType.typeName == "testHonor" && it.givenName == "JohnyHonor" && it.surName == "SmithyHonor"
        }

        then:
        person1.names && name
        name.honorifics == '["PhD"]'
        name.honorificsAsList == ["PhD"]
    }
}
