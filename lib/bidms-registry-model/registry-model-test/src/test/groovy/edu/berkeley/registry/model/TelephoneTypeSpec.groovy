package edu.berkeley.registry.model

class TelephoneTypeSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return TelephoneType }

    void "confirm TelephoneType using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm TelephoneType LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes([])
    }

    void "confirm TelephoneType logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["telephoneTypeName"])
    }
}
