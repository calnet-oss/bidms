package edu.berkeley.registry.model

class IdentifierArchiveSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return IdentifierArchive }

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

    void "confirm has hash code change callback"() {
        expect:
        testHasHashCodeChangeCallback()
    }
}
