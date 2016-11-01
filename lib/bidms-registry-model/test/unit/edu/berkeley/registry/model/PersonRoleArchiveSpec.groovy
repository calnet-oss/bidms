package edu.berkeley.registry.model

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class PersonRoleArchiveSpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return PersonRoleArchive }

    void "confirm PersonRoleArchive using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm PersonRoleArchive LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person", "roleCategory", "originalPersonRoleId", "roleAsgnUniquePerCat", "timeCreated", "timeUpdated", "endOfRoleGraceTimeOverrideFirst"])
    }

    void "confirm PersonRoleArchive logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["role", "startOfRoleGraceTime", "endOfRoleGraceTime", "endOfRoleGraceTimeOverride", "originalTimeCreated", "originalTimeUpdated", "roleInGrace", "rolePostGrace"])
    }
}
