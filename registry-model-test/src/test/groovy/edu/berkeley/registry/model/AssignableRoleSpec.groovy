package edu.berkeley.registry.model

import grails.test.mixin.TestFor

@TestFor(AssignableRole)
class AssignableRoleSpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return AssignableRole }

    void "confirm AssignableRole using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm AssignableRole LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes([])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["roleName", "roleCategory"])
    }
}
