package edu.berkeley.registry.model

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class AppointmentTypeSpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return AppointmentType }

    void "confirm AppointmentType using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm AppointmentType LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes([])
    }

    void "confirm AppointmentType logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["apptTypeName"])
    }
}
