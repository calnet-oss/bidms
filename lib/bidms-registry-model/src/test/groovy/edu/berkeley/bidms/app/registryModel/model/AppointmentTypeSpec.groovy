package edu.berkeley.registry.model

class AppointmentTypeSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return AppointmentType }

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
