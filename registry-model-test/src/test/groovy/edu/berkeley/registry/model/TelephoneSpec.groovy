package edu.berkeley.registry.model

class TelephoneSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return Telephone }

    void "confirm Telephone using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm Telephone LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person"])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["telephoneType", "sorObject", "phoneNumber", "extension"])
    }

    void "confirm has hash code change callback"() {
        expect:
        testHasHashCodeChangeCallback()
    }
}
