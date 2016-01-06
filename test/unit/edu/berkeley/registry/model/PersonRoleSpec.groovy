package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(PersonRole)
class PersonRoleSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "confirm PersonRole using LogicalEqualsAndHashCode annotation"() {
        given:
            PersonRole obj = new PersonRole()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }

    void "confirm PersonRole LogicalEqualsAndHashCode excludes"() {
        given:
            PersonRole obj = new PersonRole()
        expect:
            PersonRole.logicalHashCodeExcludes.contains("person")
    }
}
