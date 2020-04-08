package edu.berkeley.match

import edu.berkeley.registry.model.SOR
import edu.berkeley.registry.model.SORObject
import spock.lang.Specification
import spock.lang.Unroll
import grails.testing.gorm.DataTest

class SorKeyDataCommandSpec extends Specification implements DataTest {

    void setupSpec() {
        mockDomains SOR, SORObject
    }

    def setup() {
        new SORObject(sor: new SOR(name: 'SIS').save(validate: false), sorPrimaryKey: '12345').save(validate: false, flush: true)
    }

    @Unroll
    def "test that it's possible to get the SORObject, if sor and primay key are correct"() {
        when:
        def command = new SorKeyDataCommand(systemOfRecord: sor, sorPrimaryKey: sorPk)

        then:
        command.sorObject?.sor?.name == expectedSor
        command.sorObject?.sorPrimaryKey == expectedSorPk

        where:
        sor    | sorPk   | expectedSor | expectedSorPk
        'SIS'  | '12345' | 'SIS'       | '12345'
        'HRMS' | '12345' | null        | null
        'SIS'  | '23456' | null        | null
        null   | null    | null        | null
    }

    @Unroll
    def "test that get attributes returns the correct values"() {
        when:
        def command = new SorKeyDataCommand(systemOfRecord: 'SIS', sorPrimaryKey: '123', givenName: givenName, middleName: middleName, surName: surName, dateOfBirth: dateOfBirth, otherIds: otherIds, matchOnly: matchOnly)

        then:
        command.attributes.systemOfRecord == 'SIS'
        command.attributes.sorPrimaryKey == '123'
        command.attributes.keySet().sort() == expectedOutputAttributes.sort()

        where:
        givenName | middleName | surName | dateOfBirth | otherIds       | matchOnly | expectedOutputAttributes
        null      | null       | null    | null        | null           | null      | ['systemOfRecord', 'sorPrimaryKey']
        null      | null       | null    | null        | [:]            | null      | ['systemOfRecord', 'sorPrimaryKey']
        'b'       | null       | null    | 'e'         | [:]            | null      | ['systemOfRecord', 'sorPrimaryKey', 'givenName', 'dateOfBirth']
        'b'       | 'c'        | 'd'     | 'e'         | [:]            | null      | ['systemOfRecord', 'sorPrimaryKey', 'givenName', 'middleName', 'surName', 'dateOfBirth']
        'b'       | null       | null    | 'e'         | [kryf: 'plyf'] | null      | ['systemOfRecord', 'sorPrimaryKey', 'givenName', 'dateOfBirth', 'otherIds']
        null      | null       | null    | null        | null           | true      | ['systemOfRecord', 'sorPrimaryKey', 'matchOnly']
        null      | null       | null    | null        | null           | 'true'    | ['systemOfRecord', 'sorPrimaryKey', 'matchOnly']
    }
}
