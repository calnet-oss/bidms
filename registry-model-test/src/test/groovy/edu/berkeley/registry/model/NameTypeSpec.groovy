package edu.berkeley.registry.model

class NameTypeSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return NameType }

    void "confirm NameType using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm NameType LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes([])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["typeName"])
    }
}
