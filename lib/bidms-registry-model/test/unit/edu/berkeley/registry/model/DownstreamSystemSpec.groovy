package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class DownstreamSystemSpec extends Specification {
    void "confirm DownstreamSystem using LogicalEqualsAndHashCode annotation"() {
        given:
            DownstreamSystem obj = new DownstreamSystem()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }
}
