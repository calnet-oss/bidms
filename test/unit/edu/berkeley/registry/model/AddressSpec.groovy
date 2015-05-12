package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class AddressSpec extends Specification {

    void "confirm Address using LogicalEqualsAndHashCode annotation"() {
        given:
            Address obj = new Address()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }

    void "confirm Address LogicalEqualsAndHashCode excludes"() {
        given:
            Address obj = new Address()
        expect:
            Address.logicalHashCodeExcludes.contains("person")
    }
}
