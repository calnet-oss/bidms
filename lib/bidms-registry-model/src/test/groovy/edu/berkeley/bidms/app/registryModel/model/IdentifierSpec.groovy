package edu.berkeley.registry.model

class IdentifierSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return Identifier }

    void "confirm Identifier using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm Identifier LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person"])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["identifierType", "sorObject", "identifier", "isActive", "isPrimary", "weight"])
    }

    void "confirm has hash code change callback"() {
        expect:
        testHasHashCodeChangeCallback()
    }
}
