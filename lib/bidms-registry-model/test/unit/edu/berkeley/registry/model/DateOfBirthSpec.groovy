package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class DateOfBirthSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "confirm DateOfBirth using LogicalEqualsAndHashCode annotation"() {
        given:
            DateOfBirth obj = new DateOfBirth()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }

    void "confirm DateOfBirth LogicalEqualsAndHashCode excludes"() {
        given:
            DateOfBirth obj = new DateOfBirth()
        expect:
            DateOfBirth.logicalHashCodeExcludes.contains("person")
    }
}
