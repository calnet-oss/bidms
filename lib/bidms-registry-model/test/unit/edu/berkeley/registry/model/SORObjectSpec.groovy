package edu.berkeley.registry.model

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder
import spock.lang.Specification

@TestFor(SORObject)
@Mock([SORObject, SOR])
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

    def "test json"() {
        given:
            def json = new JsonBuilder([id:3, name:'archer']).toString()
            def obj = new SORObject(sor: SOR.findById(1), sorPrimaryKey: '123', objJson: json).save(flush: true, validate: false)

        expect:
            obj.json.id == 3
            obj.json.name == 'archer'
    }

}
