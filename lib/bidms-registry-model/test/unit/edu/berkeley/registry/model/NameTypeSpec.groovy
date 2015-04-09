package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class NameTypeSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "confirm NameType using LogicalEqualsAndHashCode annotation"() {
        given:
            NameType obj = new NameType()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }
}
