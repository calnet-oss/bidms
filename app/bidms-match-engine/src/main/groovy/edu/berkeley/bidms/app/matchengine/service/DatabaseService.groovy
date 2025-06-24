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
package edu.berkeley.bidms.app.matchengine.service

import edu.berkeley.bidms.app.matchengine.ConfidenceType
import edu.berkeley.bidms.app.matchengine.SearchResult
import edu.berkeley.bidms.app.matchengine.SearchSet
import edu.berkeley.bidms.app.matchengine.config.MatchConfidence
import edu.berkeley.bidms.app.matchengine.config.MatchConfig
import edu.berkeley.bidms.app.matchengine.database.Record
import edu.berkeley.bidms.app.matchengine.util.sql.WhereAndValues
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service("matchEngineDatabaseService")
@Transactional(readOnly = true)
class DatabaseService {

    SqlService sqlService
    RowMapperService rowMapperService
    MatchConfig matchConfig

    DatabaseService(SqlService sqlService, RowMapperService rowMapperService, MatchConfig matchConfig) {
        this.sqlService = sqlService
        this.rowMapperService = rowMapperService
        this.matchConfig = matchConfig
    }

    /**
     * Search the view for (fuzzy) matches based on the matchInput, the confidenceType and the matchConfig
     * @param matchInput
     * @param confidenceType
     * @return a list of Record objects, if any rows matches
     */
    Set<Record> searchDatabase(Map matchInput, ConfidenceType confidenceType) {
        def searchSets = getSearchSets(confidenceType)

        // Find where clauses that has content
        List<WhereAndValues> whereClauses = searchSets.collect { searchSet ->
            searchSet.buildWhereClause(matchInput)
        }.findAll {
            it
        }

        if (!whereClauses) {
            return []
        }

        Set<QueryStatement> sqlStatements = whereClauses.collect { whereClause ->
            new QueryStatement(
                    ruleName: whereClause.ruleName,
                    sql: """
                            SELECT *
                                FROM   ${matchConfig.matchTable}
                                WHERE  ${matchConfig.matchReference.column} IS NOT NULL
                                AND    ${whereClause.sql}
                         """.stripIndent(),
                    values: whereClause.values.flatten())
        } as Set

        def rows = performSearch(sqlStatements)

        Set<Record> records = rowMapperService.mapDataRowsToRecords(rows, confidenceType, matchInput)

        if (confidenceType == ConfidenceType.SUPERCANONICAL && !records) {
            // If there are no super-canonical results, there's still one
            // more possibility and that is that the other SORObject that
            // matches this identifier is sitting in the PartialMatch table.
            // If that's the case, then this SORObject needs to go into
            // PartialMatch too because we don't want to be creating a new
            // uid for this when the other potentially matches up to an
            // existing uid.
            rows.each { SearchResult searchResult ->
                SearchSet searchSet = searchSets.find { it.matchConfidence.ruleName == searchResult.ruleName }
                def sorMatchAttributeConfig = searchSet.matchConfidence.confidence.collect { matchTypeEntry ->
                    searchSet.matchAttributeConfigs.find { it.name == matchTypeEntry.key }
                }.find { it.attribute == "systemOfRecord" }
                def targetSorName = sorMatchAttributeConfig.search.fixedValue
                List<String> partialMatchUids = getSorObjectInPartialMatch(targetSorName, matchInput.identifier)
                if (partialMatchUids) {
                    records.addAll(partialMatchUids.collect { String uid ->
                        new Record(
                                exactMatch: false,
                                referenceId: uid,
                                ruleNames: [searchResult.ruleName]
                        )
                    })
                }
            }
        }

        return records
    }

    /**
     * Search to see if the identifier for the system of record is found. This would be an identical match.
     * @param systemOfRecord
     * @param identifier
     * @return a Record if found, null if not
     */
    Record findRecord(String systemOfRecord, String identifier) {
        def sql = sqlService.sqlInstance

        try {
            def systemOfRecordAttribute = matchConfig.matchAttributeConfigs.find { it.name == matchConfig.matchReference.systemOfRecordAttribute }
            def identifierAttribute = matchConfig.matchAttributeConfigs.find { it.name == matchConfig.matchReference.identifierAttribute }

            def query = 'SELECT * FROM ' + matchConfig.matchTable + ' WHERE ' + systemOfRecordAttribute.column + '=? AND ' + identifierAttribute.column + '=? AND ' + systemOfRecordAttribute.isPrimaryKeyColumn + '=?'
            def row = sql.firstRow(query, [systemOfRecord, identifier, true])

            return row ? new Record(referenceId: getReferenceIdFromRow(row), exactMatch: true) : null
        }
        finally {
            sql.close()
        }
    }

    // If the SORObject is in the PartialMatch table, returns the uids it is
    // potentially matching up to.
    List<String> getSorObjectInPartialMatch(String sorName, String sorObjKey) {
        String sql = """SELECT pm.personUid FROM PartialMatch pm, SORObject so, SOR sor
WHERE so.id = pm.sorObjectId AND pm.isReject = false AND sor.sorId = so.sorId
AND sor.sorName = ? AND so.sorObjKey = ?"""
        List<String> uids = []
        sqlService.sqlInstance.eachRow(sql, [sorName, sorObjKey]) { row ->
            uids << (row.personUid as String)
        }
        return uids
    }

    private List<SearchSet> getSearchSets(ConfidenceType confidenceType) {
        switch (confidenceType) {
            case ConfidenceType.SUPERCANONICAL:
                return matchConfig.superCanonicalConfidences.collect { MatchConfidence matchConfidence ->
                    new SearchSet(matchConfidence: matchConfidence, matchAttributeConfigs: matchConfig.matchAttributeConfigs)
                }
            case ConfidenceType.CANONICAL:
                return matchConfig.canonicalConfidences.collect { MatchConfidence matchConfidence ->
                    new SearchSet(matchConfidence: matchConfidence, matchAttributeConfigs: matchConfig.matchAttributeConfigs)
                }
            case ConfidenceType.POTENTIAL:
                return matchConfig.potentialConfidences.collect { confidence ->
                    new SearchSet(matchConfidence: confidence, matchAttributeConfigs: matchConfig.matchAttributeConfigs)
                }
        }
    }

    private List<SearchResult> performSearch(Set<QueryStatement> queryStatements) {
        List<SearchResult> results = queryStatements.collect { queryStatement ->
            return performSearch(queryStatement)
        }
        // If more than one searchResult stems from the same ruleName, collect the rows into one entry
        Map<String, List<SearchResult>> resultsGroupedByName = results.groupBy { it.ruleName }
        List<SearchResult> rows = resultsGroupedByName.collect { name, searchResults ->
            new SearchResult(name, searchResults.collect { it.rows }.flatten() as Set<Map>)
        }

        return rows
    }

    private SearchResult performSearch(QueryStatement queryStatement) {
        def sql = sqlService.sqlInstance
        try {
            log.debug("Performing query: $queryStatement.sql with values ${queryStatement.redactedValues}")
            def start = System.currentTimeMillis()
            List<Map> result = sql.rows(queryStatement.normalizedSql, queryStatement.values)
            if (log.isDebugEnabled()) {
                List<Map> forLogging = result.collect { it.subMap([matchConfig.matchReference.column]) }
                log.debug("--- returned: ${forLogging} in ${System.currentTimeMillis() - start} ms")
            }
            return new SearchResult(queryStatement.ruleName, result as Set)
        }
        finally {
            sql.close()
        }
    }

    private String getReferenceIdFromRow(Map<String, String> databaseRow) {
        def column = matchConfig.matchReference.column
        return databaseRow?."${column}"
    }

    @ToString(includeNames = true)
    private static class QueryStatement {
        String ruleName
        String sql
        List values

        String getNormalizedSql() {
            sql?.replaceAll(/\s+/, ' ')?.trim()
        }

        List getRedactedValues() {
            // Rather crude method of redacting last-5 SSNs and DOBs:
            // Anything that is 5 digits or in the format of yyyy-mm-dd.
            values.collect { def input ->
                if (input) {
                    // SSN: 5 digits
                    if (input.toString() =~ /^\d\d\d\d\d$/) {
                        "*****"
                    }
                    // DOB: yyyy-mm-dd
                    else if (input.toString() =~ /^\d\d\d\d-\d\d-\d\d$/) {
                        "*****-**-**"
                    } else {
                        input
                    }
                } else {
                    input
                }
            }
        }
    }
}

