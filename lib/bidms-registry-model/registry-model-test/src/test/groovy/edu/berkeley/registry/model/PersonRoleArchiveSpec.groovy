package edu.berkeley.registry.model

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Shared
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class PersonRoleArchiveSpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return PersonRoleArchive }

    void "confirm PersonRoleArchive using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm PersonRoleArchive LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person", "roleCategory", "originalPersonRoleId", "roleAsgnUniquePerCat", "timeCreated", "timeUpdated", "endOfRoleGraceTimeUseOverrideIfLater"])
    }

    void "confirm PersonRoleArchive logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["role", "startOfRoleGraceTime", "endOfRoleGraceTime", "endOfRoleGraceTimeOverride", "originalTimeCreated", "originalTimeUpdated", "roleInGrace", "rolePostGrace"])
    }

    static long fiveMinuteOfMilliseconds = 5 * 60 * 1000

    @Shared
    static final Date current = new Date()

    @Shared
    static Date earlier = new Date(current.time - fiveMinuteOfMilliseconds)

    @Shared
    static Date later = new Date(current.time + fiveMinuteOfMilliseconds)

    @Unroll
    void "test getEndOfRoleGraceTimeUseOverrideIfLater"() {
        when:
        PersonRoleArchive pra = new PersonRoleArchive(
                endOfRoleGraceTime: endOfRoleGraceTime,
                endOfRoleGraceTimeOverride: endOfRoleGraceTimeOverride
        )

        then:
        pra.endOfRoleGraceTimeUseOverrideIfLater == expectedEndOfRoleGraceTimeUseOverrideIfLater

        where:
        endOfRoleGraceTime | endOfRoleGraceTimeOverride | expectedEndOfRoleGraceTimeUseOverrideIfLater
        null               | null                       | null
        null               | current                    | current
        current            | null                       | current
        current            | current                    | current
        current            | earlier                    | current
        current            | later                      | later
    }
}
