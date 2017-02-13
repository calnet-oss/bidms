package edu.berkeley.registry.model

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class TrackStatusSpec extends AbstractDomainObjectSpec {

    Class<?> getDomainClass() { return TrackStatus }

    void "confirm TrackStatus using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm TrackStatus LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(['person', 'metaData', 'metaDataJson'])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["trackStatusType", "timeCreated", "description"])
    }

    @Unroll
    def "test metaData serialization"() {
        when: "Set new metaData"
        def sut = new TrackStatus(metaData: metaData)

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
        def sut = new TrackStatus(metaDataJson: metaDataJson)

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
