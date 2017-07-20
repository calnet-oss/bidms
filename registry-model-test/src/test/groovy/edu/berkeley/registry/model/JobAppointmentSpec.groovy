package edu.berkeley.registry.model

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class JobAppointmentSpec extends AbstractDomainObjectSpec {
    public Class<?> getDomainClass() { return JobAppointment }

    void "confirm JobAppointment using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm JobAppointment LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person", "person_"])
    }

    void "confirm JobAppointment logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["jobCode", "jobTitle", "deptCode", "deptName", "hireDate", "apptType", "sorObject", "apptIdentifier", "isPrimaryAppt", "beginDate", "endDate"])
    }

    void "confirm has hash code change callback"() {
        expect:
        testHasHashCodeChangeCallback()
    }
}
