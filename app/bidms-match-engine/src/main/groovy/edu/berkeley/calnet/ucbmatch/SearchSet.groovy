package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig
import edu.berkeley.calnet.ucbmatch.config.MatchConfidence
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.util.AttributeValueResolver
import edu.berkeley.calnet.ucbmatch.util.SqlWhereResolver
import groovy.transform.ToString
import groovy.util.logging.Log4j

@Log4j
class SearchSet {
    MatchConfidence matchConfidence
    List<MatchAttributeConfig> matchAttributeConfigs

    WhereAndValues buildWhereClause(Map matchInput) {
        List<WhereAndValue> whereAndValues = matchConfidence.confidence.collect { String name, MatchConfig.MatchType matchType ->
            def config = matchAttributeConfigs.find { it.name == name }
            if (config.input?.fixedValue && matchInput[config.attribute] != config.input.fixedValue) {
                // matchInput attribute value does not match the fixedValue
                // in the 'input' part of the config
                return null
            }
            def value = AttributeValueResolver.getAttributeValue(config, matchInput)
            def sqlValue = SqlWhereResolver.getWhereClause(matchType, config, value)
            new WhereAndValue(sqlValue)
        }
        log.trace("Found ${whereAndValues.size()} statements for ${matchConfidence.ruleName}. Now checking if all has a value")
        if (whereAndValues.every { it?.value != null }) {
            def returnValue = new WhereAndValues(ruleName: matchConfidence.ruleName, sql: whereAndValues.sql.join(' AND '), values: whereAndValues.value)
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


}

@ToString(includeNames = true)
class WhereAndValues {
    String ruleName
    String sql
    List values
}
