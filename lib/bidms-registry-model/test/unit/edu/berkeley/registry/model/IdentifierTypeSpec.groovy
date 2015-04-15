package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class IdentifierTypeSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "confirm IdentifierType using LogicalEqualsAndHashCode annotation"() {
        given:
            IdentifierType obj = new IdentifierType()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }
}
