package edu.berkeley.registry.model

class DateOfBirthSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return DateOfBirth }

    void "confirm DateOfBirth using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm DateOfBirth LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person"])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["sorObject", "dateOfBirthMMDD", "dateOfBirth"])
    }

    void "confirm has hash code change callback"() {
        expect:
        testHasHashCodeChangeCallback()
    }
}
