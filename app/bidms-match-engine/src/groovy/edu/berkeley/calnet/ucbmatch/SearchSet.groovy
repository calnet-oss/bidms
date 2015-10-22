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

    WhereAndValues buildWhereClause(Map matchInput) {
        List<WhereAndValue> whereAndValues = matchTypes.collect { name, matchType ->
            def config = matchAttributeConfigs.find { it.name == name }
            def value = AttributeValueResolver.getAttributeValue(config, matchInput)
            def sqlValue = SqlWhereResolver.getWhereClause(matchType, config, value)
            new WhereAndValue(sqlValue)
        }
        log.trace("Found ${whereAndValues.size()} statements. Now checking if all has a value")
        if (whereAndValues.every { it.value != null }) {
            def returnValue = new WhereAndValues(sql: whereAndValues.sql.join(' AND '), values: whereAndValues.value)
            log.trace("Returning search sql: $returnValue.sql with values: $returnValue.values ")
            return returnValue
        } else {
            return null
        }
    }

    @ToString(includeNames = true)
    private static class WhereAndValue {
        String sql
        def value
    }

    @ToString(includeNames = true)
    static class WhereAndValues {
        String sql
        List values
    }

}
