package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.config.MatchConfidence
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig.create
import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType.DISTANCE
import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType.EXACT
import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType.FIXED_VALUE
import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType.SUBSTRING

class SearchSetSpec extends Specification {

    @Shared
    Map matchInput = [
            systemOfRecord          : "SIS",
            identifier              : "SI12345",

            names                   : [[
                                               type  : "official",
                                               given : "Pamela",
                                               family: "Anderson"],
                                       [
                                               given : "Pam",
                                               family: "Anderson"]
            ],
            dateOfBirth             : "1983-03-18",
            identifiers             : [[
                                               type      : "national",
                                               identifier: "3B902AE12DF55196"],
                                       [
                                               type      : "enterprise",
                                               identifier: "ABCD1234"]
            ],
            nullValueAttribute1     : "    ",
            nullValueAttribute2     : "000-00-0000",
            customNullValueAttribute: "xxxxxx"

    ]


    def "test buildWhereClause for simple matchAttributeConfigs and map of attributes"() {
        setup:
        def matchAttributeConfigs = [
                create(name: 'sor', column: 'SOR', attribute: 'systemOfRecord'),
                create(name: 'sorid', column: "SORID", attribute: 'identifier')
        ]
        def confidences = [sor: EXACT, sorid: EXACT]
        def sut = new SearchSet(matchAttributeConfigs: matchAttributeConfigs, matchConfidence: new MatchConfidence(confidence: confidences, ruleName: 'name'))

        when:
        def whereClause = sut.buildWhereClause(matchInput)

        then:
        whereClause.sql == "lower(SOR)=? AND lower(SORID)=?"
        whereClause.values == [matchInput.systemOfRecord.toLowerCase(), matchInput.identifier.toLowerCase()]
    }

    def "test buildWhereClause for matchAttributeConfigs with missing attribute in map of attributes"() {
        setup:
        def matchAttributeConfigs = [
                create(name: 'sor', column: 'SOR', attribute: 'systemOfRecord'),
                create(name: 'sorid', column: "SORID", attribute: 'identifier'),
                create(name: 'missing', column: "MISSING")
        ]
        def confidences = [sor: EXACT, sorid: EXACT, missing: EXACT]

        def sut = new SearchSet(matchAttributeConfigs: matchAttributeConfigs, matchConfidence: new MatchConfidence(confidence: confidences, ruleName: 'name'))

        when:
        def whereClause = sut.buildWhereClause(matchInput)

        then:
        !whereClause
    }

    def "test buildWhereClause for advanced matchAttributeConfigs and map of attributes"() {
        setup:
        def matchAttributeConfigs = [
                create(name: 'firstName', column: "FIRST_NAME", path: 'names', attribute: 'given', group: 'official', substring: [from: 1, length: 3]),
                create(name: 'lastName', column: "LAST_NAME", path: 'names', attribute: 'family', group: 'official', distance: 3),
                create(name: 'dob', column: 'DATE_OF_BIRTH', attribute: 'dateOfBirth', alphanumeric: true, caseSensitive: true)
        ]
        def confidences = [firstName: SUBSTRING, lastName: DISTANCE, dob: EXACT]
        def sut = new SearchSet(matchAttributeConfigs: matchAttributeConfigs, matchConfidence: new MatchConfidence(confidence: confidences, ruleName: 'name'))

        when:
        def whereClause = sut.buildWhereClause(matchInput)

        then:
        whereClause.sql == "substring(lower(FIRST_NAME) from 1 for 3)=substring(? from 1 for 3) AND levenshtein_less_equal(lower(LAST_NAME),?,3)<4 AND DATE_OF_BIRTH=?"
        whereClause.values == ["pamela", "anderson", '19830318']
    }

    /**
     * When input.fixedValue is in the rule config, the rule should only
     * execute if the input.fixedValue matches what's in
     * matchInput[config.attribute].
     *
     * An example of where this is useful in the real world: When you have
     * incoming data from a SOR that shares a primary key with another SOR.
     * e.g., UCPATH_DDODS <-> UCPATH_INTER_PERUPD
     */
    @Unroll
    def "test rule execution when input.fixedValue is part of the rule config: #description"() {
        setup:
        def matchAttributeConfigs = [
                create(name: 'identifier', column: 'identifier', attribute: 'identifier', group: 'sor', outputPath: 'identifiers', search: [caseSensitive: true]),
                create(name: 'sisSor', column: 'identifiersor', attribute: 'systemOfRecord', input: [fixedValue: matchOnInputSor], search: [caseSensitive: true, fixedValue: 'OTHER_SOR'])
        ]
        def confidences = [sisSor: FIXED_VALUE, identifier: EXACT]
        def sut = new SearchSet(matchAttributeConfigs: matchAttributeConfigs, matchConfidence: new MatchConfidence(confidence: confidences, ruleName: 'name'))

        when:
        def whereClause = sut.buildWhereClause(matchInput)

        then:
        expectMatch ? whereClause.sql == "identifiersor=? AND identifier=?" : !whereClause
        expectMatch ? whereClause.values == ['OTHER_SOR', 'SI12345'] : !whereClause

        where:
        description                                    | matchOnInputSor || expectMatch
        "input matches input config input.fixedValue"  | "SIS"           || true
        "input does not match config input.fixedValue" | "PAYROLL"       || false
    }
}
