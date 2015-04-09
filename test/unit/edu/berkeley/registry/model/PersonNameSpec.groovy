package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class PersonNameSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "confirm PersonName using LogicalEqualsAndHashCode annotation"() {
        given:
            PersonName obj = new PersonName()
        when:
            boolean isInstance = obj instanceof LogicalEqualsAndHashCodeInterface
        then:
            isInstance == true
    }

    void "confirm PersonName LogicalEqualsAndHashCode excludes"() {
        given:
            PersonName obj = new PersonName()
        when:
            boolean hasExcludes = PersonName.logicalHashCodeExcludes.contains("person")
        then:
            hasExcludes == true
    }
}
