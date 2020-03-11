package edu.berkeley.registry.model

import grails.testing.gorm.DataTest
import spock.lang.Specification

/**
 * This is similar to PersonHibernateSpec, but we are testing non-hibernate
 * testing here (i.e., we extend Specification, not HibernateSpec) .
 */
class PersonSpec extends Specification implements DataTest {
    private static final Map opts = [failOnError: true, flush: true]

    void setupSpec() {
        mockDomains AppointmentType, SOR, SORObject, Person, JobAppointment
    }

    void "save test in non-Hibernate unit test environment"() {
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
        apptType.save(opts)
        sor.save(opts)
        sorObject.save(opts)
        person.save(opts)
        person.refresh()

        then:
        /**
         * Since using 'uid' as the identifier property, this confirms Person
         * has a getId() and setId() method that retrieves and sets the uid
         * property.
         */
        person.id
        /* Confirm jobAppointments collection persisted */
        person.jobAppointments?.size() > 0
        person.jobAppointments.first().apptIdentifier == "appt123"
        /* Test findById() finds the uid */
        Person.findById("uid123")
    }
}
