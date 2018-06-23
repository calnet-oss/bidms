package edu.berkeley.registry.model

class IdentifierTypeSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return IdentifierType }

    void "confirm IdentifierType using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm IdentifierType LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes([])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["idName"])
    }
}
