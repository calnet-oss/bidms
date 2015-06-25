package edu.berkeley.calnet.ucbmatch.util

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig

import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType

class SqlWhereResolver {
    static ALL_ALPHANUMERIC = /[^A-Za-z0-9]/

    static Map getWhereClause(MatchType matchType, MatchAttributeConfig config, String value) {
        def sql = config.column
        def searchConfig = config.search

        if (!searchConfig.caseSensitive) {
            sql = "lower($sql)"
            value = value?.toLowerCase()
        }
        if (searchConfig.alphanumeric) {
            sql = "regexp_replace($sql,'$ALL_ALPHANUMERIC','','g')"
            value = value?.replaceAll(ALL_ALPHANUMERIC, '')
        }

        switch (matchType) {
            case MatchType.SUBSTRING: // If type is substring check if the config has a substring setting
                sql = searchConfig.substring ? substringSql(searchConfig, sql) : exactSql(sql)
                break
            case MatchType.DISTANCE:
                def distance = searchConfig.distance // If type is distance check if the config has a distance setting
                sql = searchConfig.distance ? "levenshtein_less_equal($sql,?,$distance)<${distance + 1}" : exactSql(sql)
                break
            case MatchType.EXACT:
                sql = exactSql(sql)
                break
        }

        // TODO: Implement crosscheck (if needed)
        return [sql: sql, value: value]

    }


    private static String substringSql(MatchAttributeConfig.SearchSettings searchConfig, String sql) {
        def from = searchConfig.substring.from
        def length = searchConfig.substring.length
        "substring($sql from $from for $length)=substring(? from $from for $length)"
    }

    private static String exactSql(sql) {
        "${sql}=?"
    }
}
