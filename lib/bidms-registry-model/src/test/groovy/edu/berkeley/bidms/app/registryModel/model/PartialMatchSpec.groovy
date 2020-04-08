package edu.berkeley.registry.model

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class PartialMatchSpec extends Specification implements DomainUnitTest<PartialMatch> {

    @Unroll
    def "test metaData serialization"() {
        when: "Set new metaData"
        def sut = new PartialMatch(metaData: metaData)

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
        def sut = new PartialMatch(metaDataJson: metaDataJson)

        and: "trigger onLoad event"
        sut.afterLoad()

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
