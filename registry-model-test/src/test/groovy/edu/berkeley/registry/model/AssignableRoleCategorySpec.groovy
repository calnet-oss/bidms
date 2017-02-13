package edu.berkeley.registry.model

class AssignableRoleCategorySpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return AssignableRoleCategory }

    void "confirm AssignableRoleCategory using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm AssignableRoleCategory LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["parent"])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["categoryName", "roleAsgnUniquePerCat"])
    }
}
