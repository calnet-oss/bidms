package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class TelephoneSpec extends Specification {

    void "confirm Telephone using LogicalEqualsAndHashCode annotation"() {
        given:
            Telephone obj = new Telephone()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }

    void "confirm Telephone LogicalEqualsAndHashCode excludes"() {
        given:
            Telephone obj = new Telephone()
        expect:
            Telephone.logicalHashCodeExcludes.contains("person")
    }
}
