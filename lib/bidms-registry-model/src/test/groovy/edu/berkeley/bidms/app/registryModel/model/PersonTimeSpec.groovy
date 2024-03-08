/*
 * Copyright (c) 2024, Regents of the University of California and
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
import edu.berkeley.bidms.app.registryModel.repo.PersonTimeRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import edu.berkeley.bidms.app.registryModel.repo.TimeTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

import java.text.SimpleDateFormat

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class PersonTimeSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    TimeTypeRepository timeTypeRepository

    @Autowired
    PersonTimeRepository personTimeRepository

    Class<?> getDomainClass() { return PersonTime }

    void "confirm PersonTime is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static PersonTime[] getTestTimes(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, TimeTypeRepository timeTypeRepository) {
        TimeType timeType = timeTypeRepository.findByTimeTypeName("testTime")
        return [
                new PersonTime(
                        person: TestUtil.findPerson(personRepository, "1"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "HR_PERSON", "hr123"),
                        timeType: timeType,
                        time: new SimpleDateFormat("yyyyMMddHHmmss.SSSZ").parse("20240301092910.614-0800"),
                        sourceValue: 133537877506137535L
                )
        ]
    }

    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
        SORObjectSpec.insertSorObjects(personRepository, sorRepository, sorObjectRepository)
    }

    static synchronized void insertTimes(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, TimeTypeRepository timeTypeRepository, PersonTimeRepository personTimeRepository) {
        timeTypeRepository.saveAndFlush(new TimeType(timeTypeName: "testTime"))
        // assign right uid to the SORObjects
        [["HR_PERSON", "hr123"], ["SIS_STUDENT", "sisStudent123"], ["ALUMNI", "alumni123"]].eachWithIndex { List<String> entry, int i ->
            SORObject sorObject = TestUtil.findSORObject(sorRepository, sorObjectRepository, entry[0], entry[1])
            sorObject.person = TestUtil.findPerson(personRepository, (i + 1).toString())
            sorObjectRepository.save(sorObject)
        }
        getTestTimes(personRepository, sorRepository, sorObjectRepository, timeTypeRepository).each {
            timeTypeRepository.saveAndFlush(it)
        }
    }

    void "save test"() {
        when:
        insertTimes(personRepository, sorRepository, sorObjectRepository, timeTypeRepository, personTimeRepository)
        List<PersonTime> expected = getTestTimes(personRepository, sorRepository, sorObjectRepository, timeTypeRepository)
        List<PersonTime> actual = personTimeRepository.findAll() as List<PersonTime>

        then:
        expected == actual
    }
}
