package edu.berkeley.registry.model

class AddressSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return Address }

    void "confirm Address using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm Address LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person"])
    }

    void "confirm Address logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["addressType", "sorObject", "address1", "address2", "address3", "city", "regionState", "postalCode", "country"])
    }

    void "confirm has hash code change callback"() {
        expect:
        testHasHashCodeChangeCallback()
    }
}
