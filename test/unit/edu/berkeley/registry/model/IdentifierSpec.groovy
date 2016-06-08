package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class IdentifierSpec extends Specification {
    void "confirm Identifier using LogicalEqualsAndHashCode annotation"() {
        given:
        Identifier obj = new Identifier()
        expect:
        obj instanceof LogicalEqualsAndHashCodeInterface
    }

    void "confirm Identifier LogicalEqualsAndHashCode excludes"() {
        given:
        Identifier obj = new Identifier()
        expect:
        Identifier.logicalHashCodeExcludes == ["id", "belongsTo", "constraints", "mapping", "person", "isPrimary"]
    }

    void "confirm Identifier LogicalEqualsAndHashCode logicalHashCodeProperties"() {
        given:
        Identifier obj = new Identifier()
        println(obj.logicalHashCodeProperties)
        expect:
        Identifier.logicalHashCodeProperties == ["identifierType", "sorObject", "identifier", "isActive", "weight"]
    }
}
