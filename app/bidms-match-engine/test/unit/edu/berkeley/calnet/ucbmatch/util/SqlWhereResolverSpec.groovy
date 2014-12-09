package edu.berkeley.calnet.ucbmatch.util

import edu.berkeley.calnet.ucbmatch.MatchType
import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig
import spock.lang.Specification
import spock.lang.Unroll

class SqlWhereResolverSpec extends Specification {

    @Unroll
    def "test canonical sql generation"() {
        given:
        def config = createSearchMatchConfig(searchConfig)

        when:
        def sqlWhere = SqlWhereResolver.getWhereClause(MatchType.CANONICAL, config, 'SIS-123')

        then:
        sqlWhere.sql == expectedSql
        sqlWhere.value == expectedValue

        where:
        searchConfig                                                        | expectedSql                                             | expectedValue
        [exact: true, caseSensitive: false]                                 | 'lower(SOR)=?'                                          | 'sis-123'
        [exact: true, caseSensitive: true]                                  | 'SOR=?'                                                 | 'SIS-123'
        [exact: true, caseSensitive: true, alphanumeric: true]              | "regex_replace(SOR,'[^A-Za-z0-9]','','g')=?"            | 'SIS123'
        [exact: true, alphanumeric: true]                                   | "regex_replace(lower(SOR),'[^A-Za-z0-9]','','g')=?"     | 'sis123'
        [exact: true, caseSensitive: true, substring: [from: 1, length: 4]] | 'substring(SOR from 1 for 4)=substring(? from 1 for 4)' | 'SIS-123'
    }

    @Unroll
    def "test potential sql generation"() {
        given:
        def config = createSearchMatchConfig(searchConfig)

        when:
        def sqlWhere = SqlWhereResolver.getWhereClause(MatchType.POTENTIAL, config, 'SIS-123')

        then:
        sqlWhere.sql == expectedSql
        sqlWhere.value == expectedValue

        where:
        searchConfig                                           | expectedSql                                         | expectedValue
        [exact: true, caseSensitive: false]                    | 'lower(SOR)=?'                                      | 'sis-123'
        [exact: true, caseSensitive: true]                     | 'SOR=?'                                             | 'SIS-123'
        [exact: true, caseSensitive: true, alphanumeric: true] | "regex_replace(SOR,'[^A-Za-z0-9]','','g')=?"        | 'SIS123'
        [exact: true, alphanumeric: true]                      | "regex_replace(lower(SOR),'[^A-Za-z0-9]','','g')=?" | 'sis123'
        [exact: true, distance: 3, caseSensitive: true]        | 'levenshtein_less_equal(SOR,?,3)<4'                 | 'SIS-123'
    }


    private MatchAttributeConfig createSearchMatchConfig(Map searchSettings = [:]) {
        new MatchAttributeConfig(name: 'sor', column: 'SOR', search: new MatchAttributeConfig.SearchSettings(searchSettings))
    }


}
