package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig
import edu.berkeley.calnet.ucbmatch.util.AttributeValueResolver
import edu.berkeley.calnet.ucbmatch.util.SqlWhereResolver
import groovy.transform.ToString
import groovy.util.logging.Log4j

import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType

@Log4j
class SearchSet {
    Map<String, MatchType> matchTypes
    List<MatchAttributeConfig> matchAttributeConfigs

    Map buildWhereClause(Map matchInput) {
        List<WhereAndValue> whereAndValues = matchTypes.collect { name, matchType ->
            def config = matchAttributeConfigs.find { it.name == name }
            def value = AttributeValueResolver.getAttributeValue(config, matchInput)
            def sqlValue = SqlWhereResolver.getWhereClause(matchType, config, value)
            new WhereAndValue(sqlValue)
        }
        if (whereAndValues.every { it.value != null }) {
            return [sql: whereAndValues.sql.join(' AND '), values: whereAndValues.value]
        } else {
            return null
        }
    }

    @ToString(includeNames = true)
    private static class WhereAndValue {
        String sql
        def value
    }
}
