package edu.berkeley.calnet.ucbmatch
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Record
import grails.transaction.Transactional
import groovy.sql.GroovyRowResult
import groovy.transform.ToString

@Transactional
class DatabaseService {

    MatchConfig matchConfig
    def sqlService
    def rowMapperService

    Set<Record> searchDatabase(Map matchInput, ConfidenceType confidenceType) {
        def searchSets = getSearchSets(confidenceType)
        Set sqlStatements = searchSets.inject([] as Set) { statements, searchSet ->
            def whereClause = searchSet.buildWhereClause(matchInput)
            if (whereClause) {
                statements << new QueryStatement(sql: """
                SELECT *
                    FROM   """ + matchConfig.matchTable + """
                    WHERE  """ + matchConfig.matchReference.column + """ IS NOT NULL
                    AND    ${whereClause.sql}
             """.toString(), values: whereClause.values)
            }
            return statements
        } as Set
        def rows = performSearch(sqlStatements)
        return rowMapperService.mapDataRowsToRecords(rows, confidenceType, matchInput)
    }

    Set<Record> searchDatabase2(Map matchInput, ConfidenceType confidenceType) {
        List<SearchSet> searchSets = getSearchSets(confidenceType)

        List<SearchSet.WhereAndValues> whereClauses = searchSets.collect{ searchSet ->
            searchSet.buildWhereClause(matchInput)
        }.findAll {
            it
        }

        String sqlWhereClauses = whereClauses.collect { "($it.sql)" }.join('\n    OR      ')
        List allValues = whereClauses.collect { it.values }.flatten()

        if(sqlWhereClauses) {
            def statement = new QueryStatement(sql: """
                SELECT *
                    FROM   ${matchConfig.matchTable}
                    WHERE  ${matchConfig.matchReference.column} IS NOT NULL
                    AND    (${sqlWhereClauses})
             """.stripIndent(), values: allValues)
            def rows = performSearch(statement)

            return rowMapperService.mapDataRowsToRecords(rows, confidenceType, matchInput)
        }
        return []

    }

    Record findRecord(String systemOfRecord, String identifier, Map matchInput) {
        def sql = sqlService.sqlInstance

        def systemOfRecordAttribute = matchConfig.matchAttributeConfigs.find { it.name == matchConfig.matchReference.systemOfRecordAttribute }
        def identifierAttribute = matchConfig.matchAttributeConfigs.find { it.name == matchConfig.matchReference.identifierAttribute }

        def query = 'SELECT * FROM ' + matchConfig.matchTable + ' WHERE ' + systemOfRecordAttribute.column + '=? AND ' + identifierAttribute.column + '=? AND ' + systemOfRecordAttribute.isPrimaryKeyColumn + '=?'
        def row = sql.firstRow(query, [systemOfRecord, identifier, true])

        return row ? new Record(referenceId: getReferenceIdFromRow(row), exactMatch: true) : null
//        return row ? rowMapperService.mapDataRowToCandidate(row, ConfidenceType.CANONICAL, matchInput) : null
    }

    private List<SearchSet> getSearchSets(ConfidenceType confidenceType) {
        switch (confidenceType) {
            case ConfidenceType.CANONICAL:
                return matchConfig.canonicalConfidences.collect { matchTypes ->
                    new SearchSet(matchTypes: matchTypes, matchAttributeConfigs: matchConfig.matchAttributeConfigs)
                }
            case ConfidenceType.POTENTIAL:
                return matchConfig.potentialConfidences.collect { confidence ->
                    new SearchSet(matchTypes: confidence, matchAttributeConfigs: matchConfig.matchAttributeConfigs)
                }
        }
    }

    private Set<Map> performSearch(Set<QueryStatement> queryStatements) {
        def rows = queryStatements.collect { queryStatement ->
            return performSearch(queryStatement)
        }.flatten()
        return rows as Set
    }

    private Set<GroovyRowResult> performSearch(QueryStatement queryStatement) {
        def sql = sqlService.sqlInstance
        log.debug("Performing query: $queryStatement.sql with values $queryStatement.values")
        def start = System.currentTimeMillis()
        def result = sql.rows(queryStatement.normalizedSql, queryStatement.values)
        log.debug("--- returned: ${result} in ${System.currentTimeMillis() - start} ms")
        return result as Set
    }

    private String getReferenceIdFromRow(Map<String, String> databaseRow) {
        def column = matchConfig.matchReference.column
        return databaseRow?."${column}"
    }

    @ToString(includeNames = true)
    private static class QueryStatement {
        String sql
        List values
        String getNormalizedSql() {
            sql?.replaceAll(/\s+/,' ')?.trim()
        }
    }
}

