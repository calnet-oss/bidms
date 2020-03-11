package edu.berkeley.registry.model

import grails.testing.gorm.DomainUnitTest

class AssignableRoleSpec extends AbstractDomainObjectSpec implements DomainUnitTest<AssignableRole> {

    Class<?> getDomainClass() { return AssignableRole }

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
