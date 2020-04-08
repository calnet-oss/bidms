package edu.berkeley.registry.model

import grails.testing.gorm.DomainUnitTest

class PersonRoleSpec extends AbstractDomainObjectSpec implements DomainUnitTest<PersonRole> {

    Class<?> getDomainClass() { return PersonRole }

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
