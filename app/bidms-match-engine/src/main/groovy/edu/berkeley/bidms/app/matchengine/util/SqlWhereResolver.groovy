/*
 * Copyright (c) 2014, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.bidms.app.matchengine.util

import edu.berkeley.bidms.app.matchengine.config.MatchAttributeConfig

import static edu.berkeley.bidms.app.matchengine.config.MatchConfig.MatchType

class SqlWhereResolver {
    static ALL_ALPHANUMERIC = /[^A-Za-z0-9]/

    static Map getWhereClause(MatchType matchType, MatchAttributeConfig config, String value) {
        def sql = config.column
        def searchConfig = config.search
        def queryValue = value

        if (!searchConfig.caseSensitive && !searchConfig.dateFormat) {
            sql = "lower($sql)"
            queryValue = queryValue?.toLowerCase()
        }
        if (searchConfig.alphanumeric) {
//            sql = "regexp_replace($sql,'$ALL_ALPHANUMERIC','','g')"
            queryValue = queryValue?.replaceAll(ALL_ALPHANUMERIC, '')
        }
        if (searchConfig.dateFormat) {
            matchType = MatchType.EXACT // Is ALWAYS Exact Match SQL
            try {
                queryValue = new java.sql.Date(Date.parse(searchConfig.dateFormat, queryValue).time)
            } catch (e) {
                queryValue = null
            }
        }

        switch (matchType) {
            case MatchType.SUBSTRING: // If type is substring check if the config has a substring setting
                sql = searchConfig.substring ? substringSql(searchConfig, sql) : exactSql(sql)
                break
            case MatchType.DISTANCE:
                def distance = searchConfig.distance // If type is distance check if the config has a distance setting
                sql = searchConfig.distance ? "levenshtein_less_equal($sql,?,$distance)<${distance + 1}" : exactSql(sql)
                break
            case [MatchType.EXACT, MatchType.FIXED_VALUE]:
                sql = exactSql(sql)
                break

        }

        return [sql: sql, value: queryValue]
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
