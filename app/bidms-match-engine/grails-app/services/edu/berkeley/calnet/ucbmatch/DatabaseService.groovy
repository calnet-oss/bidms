package edu.berkeley.calnet.ucbmatch
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Candidate
import grails.transaction.Transactional

@Transactional
class DatabaseService {

    MatchConfig matchConfig
    def sqlService
    def rowMapperService

    Set<Candidate> searchDatabase(Map matchInput, ConfidenceType confidenceType) {
        def searchSets = getSearchSets(confidenceType)
        Set sqlStatements = searchSets.inject([] as Set) { statements, searchSet ->
            def whereClause = searchSet.buildWhereClause(matchInput)
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


    Candidate findRecord(String systemOfRecord, String identifier) {
        def sql = sqlService.sqlInstance
        def row = sql.firstRow("SELECT * FROM matchgrid WHERE sor='$systemOfRecord' AND sorid='$identifier'")

        return row ? rowMapperService.mapDataRowToCandidate(row, ConfidenceType.CANONICAL) : null
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

    private Set<Map> performSearch(Set<Map> queryStatements) {
        def sql = sqlService.sqlInstance
        def rows = queryStatements.collect { Map queryStatement ->
            log.debug("Performing query: $queryStatement.sql with values $queryStatement.values")
            def sqlStatement = queryStatement.sql
            def sqlValues = queryStatement.values
            def result = sql.rows(sqlStatement as String, sqlValues as Object[])
            log.debug("--- returned: ${result}")
            return result
        }.flatten()
        return rows as Set
    }
}

