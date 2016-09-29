package edu.berkeley.registry.model

import grails.test.mixin.Mock
import spock.lang.Specification
import spock.lang.Unroll

@Mock([PartialMatch, Person, SORObject])
class PartialMatchSpec extends Specification {

    def person
    def sorObject

    def setup() {
        person = new Person(uid: '1').save(flush: true, validate: false)
        sorObject = new SORObject().save(flush: true, validate: false)

    }

    @Unroll
    def "test metaData serialization"() {
        given:
        def sut = new PartialMatch(sorObject: sorObject, person: person, dateCreated: new Date())

        when: "Set new metaData"
        sut.metaData = metaData

        and: "trigger beforeValidate"
        sut.beforeValidate()

        then:
        sut.metaDataJson == expectedString

        where:
        metaData          | expectedString
        null              | '{}'
        [:]               | '{}'
        [x: 'AA']         | '{"x":"AA"}'
        [x: ['AA', 'BB']] | '{"x":["AA","BB"]}'
        [x: [1, 2, 3]]    | '{"x":[1,2,3]}'
    }

    @Unroll
    def "test metaData deserialization"() {
        when:
        def sut = new PartialMatch(sorObject: sorObject, person: person, dateCreated: new Date(), metaDataJson: metaDataJson)

        and: "trigger onLoad event"
        sut.onLoad()

        then:
        sut.metaData == expectedMetaData

        where:
        metaDataJson        | expectedMetaData
        null                | [:]
        ''                  | [:]
        '{}'                | [:]
        '{"x":"AA"}'        | [x: 'AA']
        '{"x":["AA","BB"]}' | [x: ['AA', 'BB']]
        '{"x":[1,2,3]}'     | [x: [1, 2, 3]]
    }
}
