package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class SORSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "confirm SOR using LogicalEqualsAndHashCode annotation"() {
        given:
            SOR obj = new SOR()
        when:
            boolean isInstance = obj instanceof LogicalEqualsAndHashCodeInterface
        then:
            isInstance == true
    }
}
