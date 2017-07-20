package edu.berkeley.registry.model

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class EmailSpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return Email }

    void "confirm Email using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm Email LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person", "emailAddressLowerCase"])
    }

    void "confirm Email logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["emailType", "sorObject", "emailAddress"])
    }

    void "confirm has hash code change callback"() {
        expect:
        testHasHashCodeChangeCallback()
    }
}
