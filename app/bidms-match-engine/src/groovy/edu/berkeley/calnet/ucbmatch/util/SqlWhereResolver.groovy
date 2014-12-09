package edu.berkeley.calnet.ucbmatch.util

import edu.berkeley.calnet.ucbmatch.MatchType
import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig

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
            sql = "regex_replace($sql,'$ALL_ALPHANUMERIC','','g')"
            value = value?.replaceAll(ALL_ALPHANUMERIC, '')
        }

        switch (matchType) {
            case MatchType.CANONICAL:
                if (searchConfig.substring) {
                    sql = substringSql(searchConfig, sql)
                } else {
                    sql = equalsSql(sql)
                }
                break
            case MatchType.POTENTIAL:
                if(searchConfig.distance) {
                    def distance = searchConfig.distance
                    sql = "levenshtein_less_equal($sql,?,$distance)<${distance+1}"
                } else if(searchConfig.substring) {
                    sql = substringSql(searchConfig, sql)
                } else {
                    sql = equalsSql(sql)
                }
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
    private static String equalsSql(sql) {
        "${sql}=?"
    }
}
