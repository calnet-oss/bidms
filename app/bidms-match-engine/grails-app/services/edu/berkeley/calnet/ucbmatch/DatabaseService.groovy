package edu.berkeley.calnet.ucbmatch
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.database.Record
import grails.transaction.Transactional
import groovy.sql.Sql

@Transactional
class DatabaseService {

    MatchConfig matchConfig
    def sqlService
    def rowMapperService

    Set<Candidate> searchDatabase(String systemOfRecord, String identifier, Map sorAttributes, ConfidenceType confidenceType) {
        def searchSets = getSearchSets(confidenceType)
        Set sqlStatements = searchSets.inject([] as Set) { statements, searchSet ->
            def whereClause = searchSet.buildWhereClause(systemOfRecord, identifier, sorAttributes)
            if (whereClause) {
                statements << [sql: """
                SELECT *
                    FROM   matchgrid
                    WHERE  reference_id IS NOT NULL
                    AND    ${whereClause.sql}
             """.toString(), values: whereClause.values]
            }
            return statements
        } as Set
        def rows = performSearch(sqlStatements)
        return rowMapperService.mapDataRowsToCandidates(rows, confidenceType)
    }


    Set<Map> performSearch(Set<Map> queryStatements) {
        def sql = sqlService.sqlInstance
        def rows = queryStatements.collectAll { Map queryStatement ->
            log.debug("Performing query: $queryStatement.sql with values $queryStatement.values")
            def sqlStatement = queryStatement.sql
            def sqlValues = queryStatement.values
            def result = sql.rows(sqlStatement, sqlValues)
            log.debug("--- returned: ${result}")
            return result
        }
        return rows.flatten() as Set

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

    Record findRecord(String systemOfRecord, String identifier, Sql sql = null) {
        sql = sql ?: sqlService.sqlInstance
        def row = sql.firstRow("SELECT * FROM matchgrid WHERE sor='$systemOfRecord' AND sorid='$identifier'")

        return row ? new Record(referenceId: row.reference_id) : null
    }

    boolean removeRecord(String systemOfRecord, String identifier, Sql sql = null) {
        sql = sql ?: sqlService.sqlInstance
        sql.execute("DELETE FROM matchgrid WHERE sor='$systemOfRecord' AND sorid='$identifier'")
    }

    def withTransaction(Closure closure) {
        def sql = sqlService.sqlInstance
        def result = null
        closure.delegate = this

        sql.withTransaction {
            result = closure.call(sql)
        }
        return result
    }
}

