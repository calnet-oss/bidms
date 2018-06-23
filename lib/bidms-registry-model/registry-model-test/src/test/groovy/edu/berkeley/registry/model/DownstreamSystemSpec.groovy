package edu.berkeley.registry.model

class DownstreamSystemSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return DownstreamSystem }

    void "confirm DownstreamSystem using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm DownstreamSystem LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes([])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["name"])
    }
}
