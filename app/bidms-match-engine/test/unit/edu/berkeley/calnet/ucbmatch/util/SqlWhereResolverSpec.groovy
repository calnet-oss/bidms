package edu.berkeley.calnet.ucbmatch.util

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig
import spock.lang.Specification
import spock.lang.Unroll

import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType.*

class SqlWhereResolverSpec extends Specification {

    @Unroll
    def "test canonical sql generation"() {
        given:
        def config = createSearchMatchConfig(searchConfig)

        when:
        def sqlWhere = SqlWhereResolver.getWhereClause(matchType, config, searchConfig.fixedValue ?: 'SIS-123')

        then:
        sqlWhere.sql == expectedSql
        sqlWhere.value == expectedValue

        where:
        matchType   | searchConfig                                           | expectedSql                                             | expectedValue
        EXACT       | [caseSensitive: false]                                 | 'lower(SOR)=?'                                          | 'sis-123'
        SUBSTRING   | [caseSensitive: false]                                 | 'lower(SOR)=?'                                          | 'sis-123'
        FIXED_VALUE | [caseSensitive: false, fixedValue: 'FIXED']            | 'lower(SOR)=?'                                          | 'fixed'
        EXACT       | [caseSensitive: true]                                  | 'SOR=?'                                                 | 'SIS-123'
        SUBSTRING   | [caseSensitive: true]                                  | 'SOR=?'                                                 | 'SIS-123'
        FIXED_VALUE | [caseSensitive: true, fixedValue: 'FIXED']             | 'SOR=?'                                                 | 'FIXED'
        EXACT       | [caseSensitive: true, alphanumeric: true]              | "SOR=?"                                                 | 'SIS123'
        SUBSTRING   | [caseSensitive: true, alphanumeric: true]              | "SOR=?"                                                 | 'SIS123'
        EXACT       | [alphanumeric: true]                                   | "lower(SOR)=?"                                          | 'sis123'
        SUBSTRING   | [alphanumeric: true]                                   | "lower(SOR)=?"                                          | 'sis123'
        EXACT       | [caseSensitive: true, substring: [from: 1, length: 4]] | 'SOR=?'                                                 | 'SIS-123'
        SUBSTRING   | [caseSensitive: true, substring: [from: 1, length: 4]] | 'substring(SOR from 1 for 4)=substring(? from 1 for 4)' | 'SIS-123'
    }

    @Unroll
    def "test potential sql generation"() {
        given:
        def config = createSearchMatchConfig(searchConfig)

        when:
        def sqlWhere = SqlWhereResolver.getWhereClause(matchType, config, searchConfig.fixedValue ?: value)

        then:
        sqlWhere.sql == expectedSql
        sqlWhere.value == expectedValue

        where:
        matchType   | value        | searchConfig                                                        | expectedSql                                             | expectedValue
        EXACT       | 'SIS-123'    | [caseSensitive: false]                                              | 'lower(SOR)=?'                                          | 'sis-123'
        SUBSTRING   | 'SIS-123'    | [caseSensitive: false]                                              | 'lower(SOR)=?'                                          | 'sis-123'
        DISTANCE    | 'SIS-123'    | [caseSensitive: false]                                              | 'lower(SOR)=?'                                          | 'sis-123'
        FIXED_VALUE | 'SIS-123'    | [caseSensitive: false, fixedValue: 'FIXED']                         | 'lower(SOR)=?'                                          | 'fixed'
        EXACT       | 'SIS-123'    | [caseSensitive: true]                                               | 'SOR=?'                                                 | 'SIS-123'
        SUBSTRING   | 'SIS-123'    | [caseSensitive: true]                                               | 'SOR=?'                                                 | 'SIS-123'
        DISTANCE    | 'SIS-123'    | [caseSensitive: true]                                               | 'SOR=?'                                                 | 'SIS-123'
        FIXED_VALUE | 'SIS-123'    | [caseSensitive: true, fixedValue: 'FIXED']                          | 'SOR=?'                                                 | 'FIXED'
        EXACT       | 'SIS-123'    | [caseSensitive: true, alphanumeric: true]                           | "SOR=?"                                                 | 'SIS123'
        SUBSTRING   | 'SIS-123'    | [caseSensitive: true, alphanumeric: true]                           | "SOR=?"                                                 | 'SIS123'
        DISTANCE    | 'SIS-123'    | [caseSensitive: true, alphanumeric: true]                           | "SOR=?"                                                 | 'SIS123'
        EXACT       | 'SIS-123'    | [alphanumeric: true]                                                | "lower(SOR)=?"                                          | 'sis123'
        SUBSTRING   | 'SIS-123'    | [alphanumeric: true]                                                | "lower(SOR)=?"                                          | 'sis123'
        DISTANCE    | 'SIS-123'    | [alphanumeric: true]                                                | "lower(SOR)=?"                                          | 'sis123'
        EXACT       | 'SIS-123'    | [distance: 3, caseSensitive: true]                                  | 'SOR=?'                                                 | 'SIS-123'
        SUBSTRING   | 'SIS-123'    | [distance: 3, substring: [from: 1, length: 4], caseSensitive: true] | 'substring(SOR from 1 for 4)=substring(? from 1 for 4)' | 'SIS-123'
        DISTANCE    | 'SIS-123'    | [distance: 3, caseSensitive: true]                                  | 'levenshtein_less_equal(SOR,?,3)<4'                     | 'SIS-123'
        EXACT       | '2015-10-21' | [dateFormat: 'yyyy-MM-dd']                                          | 'SOR=?'                                                 | Date.parse('yyyy-MM-dd', '2015-10-21')
        EXACT       | 'kryf'       | [dateFormat: 'yyyy-MM-dd']                                          | 'SOR=?'                                                 | null
    }


    private static MatchAttributeConfig createSearchMatchConfig(Map searchSettings = [:]) {
        new MatchAttributeConfig(name: 'sor', column: 'SOR', search: new MatchAttributeConfig.SearchSettings(searchSettings))
    }


}
