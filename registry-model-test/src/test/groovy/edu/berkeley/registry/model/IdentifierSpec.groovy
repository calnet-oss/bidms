package edu.berkeley.registry.model

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class IdentifierSpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return Identifier }

    void "confirm Identifier using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm Identifier LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person"])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["identifierType", "sorObject", "identifier", "isActive", "isPrimary", "weight"])
    }
}
