package edu.berkeley.registry.model

class EmailTypeSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return EmailType }

    void "confirm EmailType using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm EmailType LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes([])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["emailTypeName"])
    }
}
