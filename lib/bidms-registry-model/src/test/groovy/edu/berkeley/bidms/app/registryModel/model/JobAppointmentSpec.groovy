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
import edu.berkeley.bidms.app.registryModel.repo.JobAppointmentRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class JobAppointmentSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    AppointmentTypeRepository appointmentTypeRepository

    @Autowired
    JobAppointmentRepository jobAppointmentRepository

    Class<?> getDomainClass() { return JobAppointment }

    void "confirm JobAppointment is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static JobAppointment[] getTestJobAppointments(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, AppointmentTypeRepository appointmentTypeRepository) {
        return [
                new JobAppointment(
                        person: TestUtil.findPerson(personRepository, "1"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "HR_PERSON", "hr123"),
                        apptType: appointmentTypeRepository.findByApptTypeName("payrollJob"),
                        apptIdentifier: '0',
                        isPrimaryAppt: false,
                        beginDate: new Date(1471034800000L),
                        endDate: new Date(1471034900000L),
                        jobCode: 'JOB_A',
                        jobTitle: 'Specialist',
                        deptCode: 'SI',
                        deptName: 'Department of Some Importance',
                        hireDate: new Date(1471034800000L)
                ),
                new JobAppointment(
                        person: TestUtil.findPerson(personRepository, "2"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "HR_PERSON", "hr124"),
                        apptType: appointmentTypeRepository.findByApptTypeName("payrollJob"),
                        apptIdentifier: '1',
                        isPrimaryAppt: true,
                        beginDate: new Date(1471034800000L),
                        endDate: new Date(1471034900000L),
                        jobCode: 'JOB_B',
                        jobTitle: 'Extra Specialist',
                        deptCode: 'EI',
                        deptName: 'Department of Extreme Importance',
                        hireDate: new Date(1471034800000L)
                ),
                new JobAppointment(
                        person: TestUtil.findPerson(personRepository, "3"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "HR_PERSON", "hr125"),
                        apptType: appointmentTypeRepository.findByApptTypeName("payrollJob"),
                        apptIdentifier: '2',
                        isPrimaryAppt: false,
                )
        ]
    }


    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
        SORObjectSpec.insertSorObjects(personRepository, sorRepository, sorObjectRepository)
    }

    static synchronized void insertJobAppointments(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, AppointmentTypeRepository appointmentTypeRepository, JobAppointmentRepository jobAppointmentRepository) {
        appointmentTypeRepository.save(new AppointmentType(apptTypeName: "payrollJob"))

        // assign right uid to the SORObjects
        [["HR_PERSON", "hr123"], ["HR_PERSON", "hr124"], ["HR_PERSON", "hr125"]].eachWithIndex { List<String> entry, int i ->
            SORObject sorObject = TestUtil.findSORObject(sorRepository, sorObjectRepository, entry[0], entry[1])
            sorObject.person = TestUtil.findPerson(personRepository, (i + 1).toString())
            sorObjectRepository.save(sorObject)
        }

        getTestJobAppointments(personRepository, sorRepository, sorObjectRepository, appointmentTypeRepository).each {
            jobAppointmentRepository.saveAndFlush(it)
        }
    }

    void "save test"() {
        when:
        insertJobAppointments(personRepository, sorRepository, sorObjectRepository, appointmentTypeRepository, jobAppointmentRepository)
        List<JobAppointment> expected = getTestJobAppointments(personRepository, sorRepository, sorObjectRepository, appointmentTypeRepository)
        List<JobAppointment> actual = jobAppointmentRepository.findAll() as List<JobAppointment>

        then:
        expected == actual
    }
}
