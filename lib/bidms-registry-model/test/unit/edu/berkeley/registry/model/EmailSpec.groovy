package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class EmailSpec extends Specification {

    void "confirm Email using LogicalEqualsAndHashCode annotation"() {
        given:
            Email obj = new Email()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }

    void "confirm Email LogicalEqualsAndHashCode excludes"() {
        given:
            Email obj = new Email()
        expect:
            Email.logicalHashCodeExcludes.contains("person")
    }
}
