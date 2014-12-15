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
        def sqlWhere = SqlWhereResolver.getWhereClause(matchType, config, 'SIS-123')

        then:
        sqlWhere.sql == expectedSql
        sqlWhere.value == expectedValue

        where:
        matchType | searchConfig                                           | expectedSql                                             | expectedValue
        EXACT     | [caseSensitive: false]                                 | 'lower(SOR)=?'                                          | 'sis-123'
        SUBSTRING | [caseSensitive: false]                                 | 'lower(SOR)=?'                                          | 'sis-123'
        EXACT     | [caseSensitive: true]                                  | 'SOR=?'                                                 | 'SIS-123'
        SUBSTRING | [caseSensitive: true]                                  | 'SOR=?'                                                 | 'SIS-123'
        EXACT     | [caseSensitive: true, alphanumeric: true]              | "regexp_replace(SOR,'[^A-Za-z0-9]','','g')=?"           | 'SIS123'
        SUBSTRING | [caseSensitive: true, alphanumeric: true]              | "regexp_replace(SOR,'[^A-Za-z0-9]','','g')=?"           | 'SIS123'
        EXACT     | [alphanumeric: true]                                   | "regexp_replace(lower(SOR),'[^A-Za-z0-9]','','g')=?"    | 'sis123'
        SUBSTRING | [alphanumeric: true]                                   | "regexp_replace(lower(SOR),'[^A-Za-z0-9]','','g')=?"    | 'sis123'
        EXACT     | [caseSensitive: true, substring: [from: 1, length: 4]] | 'SOR=?'                                                 | 'SIS-123'
        SUBSTRING | [caseSensitive: true, substring: [from: 1, length: 4]] | 'substring(SOR from 1 for 4)=substring(? from 1 for 4)' | 'SIS-123'
    }

    @Unroll
    def "test potential sql generation"() {
        given:
        def config = createSearchMatchConfig(searchConfig)

        when:
        def sqlWhere = SqlWhereResolver.getWhereClause(matchType, config, 'SIS-123')

        then:
        sqlWhere.sql == expectedSql
        sqlWhere.value == expectedValue

        where:
        matchType | searchConfig                                                        | expectedSql                                             | expectedValue
        EXACT     | [caseSensitive: false]                                              | 'lower(SOR)=?'                                          | 'sis-123'
        SUBSTRING | [caseSensitive: false]                                              | 'lower(SOR)=?'                                          | 'sis-123'
        DISTANCE  | [caseSensitive: false]                                              | 'lower(SOR)=?'                                          | 'sis-123'
        EXACT     | [caseSensitive: true]                                               | 'SOR=?'                                                 | 'SIS-123'
        SUBSTRING | [caseSensitive: true]                                               | 'SOR=?'                                                 | 'SIS-123'
        DISTANCE  | [caseSensitive: true]                                               | 'SOR=?'                                                 | 'SIS-123'
        EXACT     | [caseSensitive: true, alphanumeric: true]                           | "regexp_replace(SOR,'[^A-Za-z0-9]','','g')=?"           | 'SIS123'
        SUBSTRING | [caseSensitive: true, alphanumeric: true]                           | "regexp_replace(SOR,'[^A-Za-z0-9]','','g')=?"           | 'SIS123'
        DISTANCE  | [caseSensitive: true, alphanumeric: true]                           | "regexp_replace(SOR,'[^A-Za-z0-9]','','g')=?"           | 'SIS123'
        EXACT     | [alphanumeric: true]                                                | "regexp_replace(lower(SOR),'[^A-Za-z0-9]','','g')=?"    | 'sis123'
        SUBSTRING | [alphanumeric: true]                                                | "regexp_replace(lower(SOR),'[^A-Za-z0-9]','','g')=?"    | 'sis123'
        DISTANCE  | [alphanumeric: true]                                                | "regexp_replace(lower(SOR),'[^A-Za-z0-9]','','g')=?"    | 'sis123'
        EXACT     | [distance: 3, caseSensitive: true]                                  | 'SOR=?'                                                 | 'SIS-123'
        SUBSTRING | [distance: 3, substring: [from: 1, length: 4], caseSensitive: true] | 'substring(SOR from 1 for 4)=substring(? from 1 for 4)' | 'SIS-123'
        DISTANCE  | [distance: 3, caseSensitive: true]                                  | 'levenshtein_less_equal(SOR,?,3)<4'                     | 'SIS-123'
    }


    private static MatchAttributeConfig createSearchMatchConfig(Map searchSettings = [:]) {
        new MatchAttributeConfig(name: 'sor', column: 'SOR', search: new MatchAttributeConfig.SearchSettings(searchSettings))
    }


}
