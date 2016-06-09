package edu.berkeley.registry.model

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class AddressTypeSpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return AddressType }

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
