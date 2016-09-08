package edu.berkeley.registry.model

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class IdentifierArchiveSpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return IdentifierArchive }

    void "confirm IdentifierArchive using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm IdentifierArchive LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person"])
    }

    void "confirm IdentifierArchive logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["identifierType", "originalSorObjectId", "identifier", "wasActive", "wasPrimary", "oldWeight"])
    }
}
