package edu.berkeley.registry.model

import grails.test.mixin.TestFor

@TestFor(PersonRole)
class PersonRoleSpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return PersonRole }

    void "confirm PersonRole using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm PersonRole LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person", "roleCategory", "roleAsgnUniquePerCat"])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["role", "roleValue"])
    }

    void "confirm has hash code change callback"() {
        expect:
        testHasHashCodeChangeCallback()
    }
}
