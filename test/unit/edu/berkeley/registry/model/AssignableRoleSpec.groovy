package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(AssignableRole)
class AssignableRoleSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "confirm AssignableRole using LogicalEqualsAndHashCode annotation"() {
        given:
            AssignableRole obj = new AssignableRole()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }
}
