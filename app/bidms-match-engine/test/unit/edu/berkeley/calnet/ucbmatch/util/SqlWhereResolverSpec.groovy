package edu.berkeley.calnet.ucbmatch.util

import edu.berkeley.calnet.ucbmatch.MatchType
import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig
import spock.lang.Specification

class SqlWhereResolverSpec extends Specification {
    def "test canonical exact match sql"() {
        given:
        def config = new MatchAttributeConfig(name: 'sor', column: 'SOR', search: new MatchAttributeConfig.SearchSettings(exact: true))

        when:
        def sqlWhere = SqlWhereResolver.getWhereClause(MatchType.CANONICAL, config,'SIS')

        then:
        sqlWhere.sql == "SOR=?"
        sqlWhere.value == "SIS"

    }
}
