package edu.berkeley.registry.model

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class PersonNameSpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return PersonName }

    void "confirm PersonName using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm PersonName LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person", "honorificsAsList"])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["nameType", "sorObject", "prefix", "givenName", "middleName", "surName", "suffix", "fullName", "honorifics", "isPrimary"])
    }

    void "test honorificsAsMap"() {
        given:
        PersonName obj = new PersonName()
        obj.setHonorificsAsList([
                "JD",
                "PhD"
        ])
        expect:
        obj.honorifics == '["JD","PhD"]'
        obj.honorificsAsList == [
                "JD",
                "PhD"
        ]
    }
}
