package edu.berkeley.calnet.ucbmatch.util

import edu.berkeley.calnet.ucbmatch.MatchType
import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig

class SqlWhereResolver {
    static SqlWhere getWhereClause(MatchType matchType, MatchAttributeConfig config, String value) {
        def sqlWhere = new SqlWhere(sql: config.column, value: value)
        def searchConfig = config.search
        if(matchType == MatchType.CANONICAL && searchConfig.exact) {
            sqlWhere.sql = "${sqlWhere.sql}=?"
            return sqlWhere
        }
    }

    static class SqlWhere {
        String sql
        String value
    }
}
