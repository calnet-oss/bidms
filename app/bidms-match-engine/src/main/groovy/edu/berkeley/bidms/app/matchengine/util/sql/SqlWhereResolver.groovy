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
package edu.berkeley.bidms.app.matchengine.util.sql

import edu.berkeley.bidms.app.matchengine.config.MatchAttributeConfig
import groovy.util.logging.Slf4j

import static edu.berkeley.bidms.app.matchengine.config.MatchConfig.MatchType

class SqlWhereResolver {
    static ALL_ALPHANUMERIC = /[^A-Za-z0-9]/

    // If you did something other than this:
    // CREATE EXTENSION IF NOT EXISTS fuzzystrmatch WITH SCHEMA public
    // then you'll have to alter fuzzySchema.
    //static String fuzzySchema = "public"

    @Slf4j
    private static class QueryBuilderForWhereClause<T> {
        MatchType matchType
        final MatchAttributeConfig config
        T queryValue

        final MatchAttributeConfig.InputSettings inputConfig
        final MatchAttributeConfig.SearchSettings searchConfig

        String sql

        QueryBuilderForWhereClause(MatchType matchType, MatchAttributeConfig config, T queryValue) {
            this.matchType = matchType
            this.config = config
            this.queryValue = queryValue
            this.inputConfig = config.input
            this.searchConfig = config.search
        }

        QueryBuilderForWhereClause build() {
            sql = config.column

            boolean isInputList = inputConfig?.list || inputConfig?.stringList
            if (isInputList && !(queryValue instanceof List)) {
                throw new IllegalStateException("input is configured as a list but the incoming matchInput value is not a list")
            } else if (queryValue instanceof List && !isInputList) {
                throw new IllegalStateException("incoming matchInput value is a list but input is not configured to be a list")
            }

            if (!searchConfig?.caseSensitive && !searchConfig?.dateFormat) {
                sql = "lower($sql)"
                if (queryValue instanceof List) {
                    queryValue = (T) queryValue.collect { it.toString()?.toLowerCase() }
                } else {
                    queryValue = (T) queryValue?.toString()?.toLowerCase()
                }
            }
            if (searchConfig?.alphanumeric) {
                if (queryValue instanceof List) {
                    queryValue = (T) queryValue.collect { alphanumeric(it?.toString()) }
                } else {
                    queryValue = (T) alphanumeric(queryValue?.toString())
                }
            }
            if (searchConfig?.dateFormat) {
                if (matchType != MatchType.EXACT) {
                    // must always be exact match SQL
                    throw new IllegalStateException("${config.name} is configured with a dateFormat.  Date formatted attributes must be EXACT string matches.  You should change your match configuration to be EXACT for this rule.")
                }
                if (queryValue instanceof List) {
                    queryValue = (T) queryValue.collect {
                        dateFormat(searchConfig.dateFormat, it?.toString())
                    }
                } else {
                    queryValue = (T) dateFormat(searchConfig.dateFormat, queryValue?.toString())
                }
            }

            if (isInputList) {
                switch (matchType) {
                    case MatchType.EXACT:
                        sql = exactInSql(sql, (List) queryValue)
                        break
                    default:
                        throw new UnsupportedOperationException("$matchType is not supported for a list attribute")
                }
            } else {
                switch (matchType) {
                    case MatchType.SUBSTRING: // If type is substring check if the config has a substring setting
                        sql = searchConfig?.substring ? substringSql(searchConfig, sql) : exactSql(sql)
                        break
                    case MatchType.DISTANCE:
                        def distance = searchConfig.distance // If type is distance check if the config has a distance setting
                        sql = searchConfig?.distance ? "levenshtein_less_equal($sql,?,$distance)<${distance + 1}" : exactSql(sql)
                        break
                    case [MatchType.EXACT, MatchType.FIXED_VALUE]:
                        sql = exactSql(sql)
                        break
                    default:
                        throw new UnsupportedOperationException("$matchType is not supported")
                }
            }

            return this
        }

        private static String substringSql(MatchAttributeConfig.SearchSettings searchConfig, String sql) {
            def from = searchConfig.substring.from
            def length = searchConfig.substring.length
            "substring($sql from $from for $length)=substring(? from $from for $length)"
        }

        private static String exactSql(String sql) {
            "${sql}=?"
        }

        private static String exactInSql(String sql, List values) {
            "${sql} IN (${values.collect { '?' }.join(',')})"
        }

        private static String alphanumeric(String str) {
            return str?.replaceAll(ALL_ALPHANUMERIC, '')
        }

        private static java.sql.Date dateFormat(String dateFormat, String dateStr) {
            try {
                return new java.sql.Date(Date.parse(dateFormat, dateStr).time)
            } catch (ignored) {
                return null
            }
        }
    }

    static WhereAndValue getWhereClause(MatchType matchType, MatchAttributeConfig config, String value) {
        QueryBuilderForWhereClause qb = new QueryBuilderForWhereClause<String>(matchType, config, value).build()
        return new WhereAndValue(sql: qb.sql, value: qb.queryValue)
    }

    static WhereAndValueList getWhereListClause(MatchType matchType, MatchAttributeConfig config, List values) {
        // The list of values must not be empty because SQL does not support "IN ()".  The caller must ensure this.
        if (!values) {
            throw new IllegalArgumentException("values may not be null or empty because SQL does not support this.  This should be checked before calling this method.")
        }
        QueryBuilderForWhereClause qb = new QueryBuilderForWhereClause<List>(matchType, config, values).build()
        return new WhereAndValueList(sql: qb.sql, value: qb.queryValue)
    }
}
