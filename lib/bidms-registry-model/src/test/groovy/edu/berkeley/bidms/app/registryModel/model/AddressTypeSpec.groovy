package edu.berkeley.registry.model

class AddressTypeSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return AddressType }

    void "confirm AddressType using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm AddressType LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes([])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["addressTypeName"])
    }
}
