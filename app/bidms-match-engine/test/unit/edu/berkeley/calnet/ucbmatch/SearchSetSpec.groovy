package edu.berkeley.calnet.ucbmatch

import spock.lang.Shared
import spock.lang.Specification

import static edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig.create

class SearchSetSpec extends Specification {

    @Shared
            systemOfRecord = 'SIS'
    @Shared
            identifier = 'SIS12345'
    @Shared
            sorAttributes = [
                    "names"      : [
                            ["type"  : "official",
                             "given" : "Pamela",
                             "family": "Anderson"],
                            ["given" : "Pam",
                             "family": "Anderson"]

                    ],
                    "dateOfBirth": "1983-03-18",
                    "identifiers": [
                            ["type"      : "national",
                             "identifier": "3B902AE12DF55196"],
                            ["type"      : "enterprise",
                             "identifier": "ABCD1234"]
                    ]
            ]


    def "test buildWhereClause for simple matchAttributeConfigs and map of attributes"() {
        setup:
        def matchAttributeConfigs = [
                create(name: 'sor', column: 'SOR', property: 'systemOfRecord', exact: true),
                create(name: 'sorid', column: "SORID", property: 'identifier',exact: true)
        ]
        def sut = new SearchSet(matchAttributeConfigs: matchAttributeConfigs, matchType: MatchType.CANONICAL)

        when:
        def whereClause = sut.buildWhereClause(systemOfRecord, identifier, sorAttributes)

        then:
        whereClause.sql == "lower(SOR)=? AND lower(SORID)=?"
        whereClause.values == [systemOfRecord.toLowerCase(), identifier.toLowerCase()]
    }

    def "test buildWhereClause for matchAttributeConfigs with missing attribute in map of attributes"() {
        setup:
        def matchAttributeConfigs = [
                create(name: 'sor', column: 'SOR', property: 'systemOfRecord', exact: true),
                create(name: 'sorid', column: "SORID", property: 'identifier', exact: true),
                create(name: 'missing', column: "MISSING", exact: true)
        ]
        def sut = new SearchSet(matchAttributeConfigs: matchAttributeConfigs, matchType: MatchType.CANONICAL)

        when:
        def whereClause = sut.buildWhereClause(systemOfRecord, identifier, sorAttributes)

        then:
        !whereClause.sql
        !whereClause.values
    }

    def "test buildWhereClause for advanced matchAttributeConfigs and map of attributes"() {
        setup:
        def matchAttributeConfigs = [
                create(name: 'firstName', column: "FIRST_NAME", path: 'names', attribute: 'given', group: 'official', substring: [from: 1,length: 3]),
                create(name: 'lastName', column: "LAST_NAME", path: 'names', attribute: 'family', group: 'official', distance: 3),
                create(name: 'dob', column: 'DATE_OF_BIRTH', attribute: 'dateOfBirth', alphanumeric: true, caseSensitive: true)
        ]
        def sut = new SearchSet(matchAttributeConfigs: matchAttributeConfigs, matchType: MatchType.POTENTIAL)

        when:
        def whereClause = sut.buildWhereClause(systemOfRecord, identifier, sorAttributes)

        then:
        whereClause.sql == "substring(lower(FIRST_NAME) from 1 for 3)=substring(? from 1 for 3) AND levenshtein_less_equal(lower(LAST_NAME),?,3)<4 AND regex_replace(DATE_OF_BIRTH,'[^A-Za-z0-9]','','g')=?"
        whereClause.values == ["pamela", "anderson",'19830318']
    }


}
