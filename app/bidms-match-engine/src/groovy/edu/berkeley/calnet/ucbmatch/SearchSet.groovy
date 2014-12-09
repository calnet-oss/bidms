package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig
import edu.berkeley.calnet.ucbmatch.util.AttributeValueResolver
import edu.berkeley.calnet.ucbmatch.util.SqlWhereResolver
import groovy.util.logging.Log4j

@Log4j
class SearchSet {
    MatchType matchType
    List<MatchAttributeConfig> matchAttributeConfigs

    Map buildWhereClause(String systemOfRecord, String identifier, Map attributes) {
        List statements = []
        for(config in matchAttributeConfigs) {
            def value = AttributeValueResolver.getAttributeValue(config, systemOfRecord, identifier, attributes)
            if(!value) {
                return [:]
            }
            statements << SqlWhereResolver.getWhereClause(matchType, config, value)
        }

        return [sql: statements.sql.join(' AND ') , values: statements.value]

    }
}
