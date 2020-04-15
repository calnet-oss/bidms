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
package edu.berkeley.bidms.app.registryModel.model


import edu.berkeley.bidms.app.registryModel.repo.DateOfBirthRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

import java.text.SimpleDateFormat

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class DateOfBirthSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    DateOfBirthRepository dateOfBirthRepository

    Class<?> getDomainClass() { return DateOfBirth }

    void "confirm DateOfBirth is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static DateOfBirth[] getTestDates(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy")
        return [
                new DateOfBirth(
                        person: TestUtil.findPerson(personRepository, "1"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "HR_PERSON", "hr123"),
                        dateOfBirthMMDD: "0301",
                        dateOfBirth: dateFormat.parse("03/01/1999")

                ),
                new DateOfBirth(
                        person: TestUtil.findPerson(personRepository, "2"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "SIS_STUDENT", "sisStudent123"),
                        dateOfBirthMMDD: "0405"
                ),
                new DateOfBirth(
                        person: TestUtil.findPerson(personRepository, "3"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "ALUMNI", "alumni123"),
                        dateOfBirth: dateFormat.parse("07/25/1930")
                )
        ]
    }

    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
        SORObjectSpec.insertSorObjects(personRepository, sorRepository, sorObjectRepository)
    }

    static synchronized void insertDates(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, DateOfBirthRepository dateOfBirthRepository) {
        // assign right uid to the SORObjects
        [["HR_PERSON", "hr123"], ["SIS_STUDENT", "sisStudent123"], ["ALUMNI", "alumni123"]].eachWithIndex { List<String> entry, int i ->
            SORObject sorObject = TestUtil.findSORObject(sorRepository, sorObjectRepository, entry[0], entry[1])
            sorObject.person = TestUtil.findPerson(personRepository, (i + 1).toString())
            sorObjectRepository.save(sorObject)
        }
        getTestDates(personRepository, sorRepository, sorObjectRepository).each {
            dateOfBirthRepository.saveAndFlush(it)
        }
    }

    void "save test"() {
        when:
        insertDates(personRepository, sorRepository, sorObjectRepository, dateOfBirthRepository)
        List<DateOfBirth> expected = getTestDates(personRepository, sorRepository, sorObjectRepository)
        List<DateOfBirth> actual = dateOfBirthRepository.findAll() as List<DateOfBirth>

        then:
        expected == actual
    }
}
