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
        testExcludes(["person"])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["emailType", "sorObject", "emailAddress"])
    }
}
