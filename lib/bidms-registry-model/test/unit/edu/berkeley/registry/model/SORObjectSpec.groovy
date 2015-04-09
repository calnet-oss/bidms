package edu.berkeley.registry.model

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(SORObject)
@Mock([Person, PartialMatch, SORObject, PersonName, NameType, SOR])
class SORObjectSpec extends Specification {
    SOR testSOR

    def setup() {
        testSOR = new SOR(name: "SIS")
        testSOR.save(flush: true, failOnError: true)
    }

    def "test that a SORObject can be found when exists"() {
        given:
        assert SOR.get(testSOR.id) != null
        def obj = new SORObject(sor: SOR.findById(testSOR.id), sorPrimaryKey: '123').save(flush: true, validate: false, failOnError: true)

        expect:
        obj.id

        println SORObject.list()*.dump()

        and:
        SORObject.getBySorAndObjectKey('SIS', '123')
    }

    def "test that finding a SORObject with unknown sor or key returns null"() {
        given:
        def obj = new SORObject(sor: SOR.findById(1), sorPrimaryKey: '123').save(flush: true, validate: false)

        expect:
        obj.id

        and:
        !SORObject.getBySorAndObjectKey('HR','123')

        and:
        !SORObject.getBySorAndObjectKey('SIS','321')
    }
}
