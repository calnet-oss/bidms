package edu.berkeley.registry.model

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class AddressSpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return Address }

    void "confirm Address using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm Address LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person"])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["addressType", "sorObject", "address1", "address2", "address3", "city", "regionState", "postalCode", "country"])
    }
}
