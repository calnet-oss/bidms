package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.config.MatchConfidence
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Record
import grails.gorm.transactions.Transactional
import groovy.transform.ToString

@Transactional(readOnly = true)
class DatabaseService {

    MatchConfig matchConfig
    def sqlService
    def rowMapperService

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
                    values: whereClause.values)
        } as Set

        def rows = performSearch(sqlStatements)
        return rowMapperService.mapDataRowsToRecords(rows, confidenceType, matchInput)
    }

    /**
     * Search to see if the identifier for the system of record is found. This would be an identical match.
     * @param systemOfRecord
     * @param identifier
     * @return a Record if found, null if not
     */
    Record findRecord(String systemOfRecord, String identifier) {
        def sql = sqlService.sqlInstance

        def systemOfRecordAttribute = matchConfig.matchAttributeConfigs.find { it.name == matchConfig.matchReference.systemOfRecordAttribute }
        def identifierAttribute = matchConfig.matchAttributeConfigs.find { it.name == matchConfig.matchReference.identifierAttribute }

        def query = 'SELECT * FROM ' + matchConfig.matchTable + ' WHERE ' + systemOfRecordAttribute.column + '=? AND ' + identifierAttribute.column + '=? AND ' + systemOfRecordAttribute.isPrimaryKeyColumn + '=?'
        def row = sql.firstRow(query, [systemOfRecord, identifier, true])

        return row ? new Record(referenceId: getReferenceIdFromRow(row), exactMatch: true) : null
    }

    private List<SearchSet> getSearchSets(ConfidenceType confidenceType) {
        switch (confidenceType) {
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
        log.debug("Performing query: $queryStatement.sql with values $queryStatement.values")
        def start = System.currentTimeMillis()
        List<Map> result = sql.rows(queryStatement.normalizedSql, queryStatement.values)
        if (log.isDebugEnabled()) {
            List<Map> forLogging = result.collect { it.subMap([matchConfig.matchReference.column]) }
            log.debug("--- returned: ${forLogging} in ${System.currentTimeMillis() - start} ms")
        }
        return new SearchResult(queryStatement.ruleName, result as Set)
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
    }
}

