package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class TrackStatusSpec extends Specification {

    void "confirm TrackStatus using LogicalEqualsAndHashCode annotation"() {
        given:
            TrackStatus obj = new TrackStatus()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }

    void "confirm TrackStatus LogicalEqualsAndHashCode excludes"() {
        given:
            TrackStatus obj = new TrackStatus()
        expect:
            TrackStatus.logicalHashCodeExcludes.contains("person")
    }
}
