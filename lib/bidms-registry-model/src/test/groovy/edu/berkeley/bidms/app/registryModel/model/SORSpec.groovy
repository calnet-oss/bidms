package edu.berkeley.registry.model

class SORSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return SOR }

    void "confirm SOR using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm SOR LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes([])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["name"])
    }
}
