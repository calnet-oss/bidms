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


import edu.berkeley.bidms.app.registryModel.repo.AppointmentTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class PersonSpec extends Specification {

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    AppointmentTypeRepository appointmentTypeRepository

    @Autowired
    PersonRepository personRepository

    static Person[] getTestPeople() {
        return [
                new Person(uid: "1"),
                new Person(uid: "2"),
                new Person(uid: "3")
        ]
    }

    static synchronized void insertPeople(PersonRepository repo) {
        testPeople.each { person ->
            repo.save(person)
        }
    }

    void "save test"() {
        given:
        AppointmentType apptType = new AppointmentType(apptTypeName: "testApptType")
        SOR sor = new SOR(name: "HRMS")
        SORObject sorObject = new SORObject(sorPrimaryKey: "123", jsonVersion: 1, queryTime: Date.parse("yyyy-MM-dd", "2015-07-15"), sor: sor, objJson: "{}")
        Person person = new Person(uid: "uid123")
        person.addToJobAppointments(new JobAppointment(
                apptIdentifier: "appt123",
                jobCode: "TEST",
                hireDate: Date.parse("yyyy-MM-dd", "2017-02-27"),
                sorObject: sorObject,
                apptType: apptType,
                person: person
        ))

        when:
        appointmentTypeRepository.save(apptType)
        sorRepository.save(sor)
        sorObjectRepository.save(sorObject)
        personRepository.saveAndFlush(person)

        then:
        // Since using 'uid' as the identifier property, this confirms Person
        // has a getId() and setId() method that retrieves and sets the uid
        // property.
        person.id
        // Confirm jobAppointments collection persisted
        person.jobAppointments?.size() > 0
        person.jobAppointments.first().apptIdentifier == "appt123"
        // Test findById() finds the uid
        personRepository.findById("uid123")
    }

    void "delete test"() {
        given:
        AppointmentType apptType = new AppointmentType(apptTypeName: "testApptType")
        SOR sor = new SOR(name: "HRMS")
        SORObject sorObject = new SORObject(sorPrimaryKey: "123", jsonVersion: 1, queryTime: Date.parse("yyyy-MM-dd", "2015-07-15"), sor: sor, objJson: "{}")
        Person person = new Person(uid: "uid123")
        person.addToJobAppointments(new JobAppointment(
                apptIdentifier: "appt123",
                jobCode: "TEST",
                hireDate: Date.parse("yyyy-MM-dd", "2017-02-27"),
                sorObject: sorObject,
                apptType: apptType,
                person: person
        ))

        when:
        appointmentTypeRepository.save(apptType)
        sorRepository.save(sor)
        sorObjectRepository.save(sorObject)
        personRepository.save(person)
        personRepository.delete(person)

        then:
        !personRepository.get("uid123")
    }
}
