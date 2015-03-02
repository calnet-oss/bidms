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
                    FROM   """+matchConfig.matchTable+"""
                    WHERE  """+matchConfig.matchReference.column+""" IS NOT NULL
                    AND    ${whereClause.sql}
             """.toString(), values: whereClause.values]
            }
            return statements
        } as Set
        def rows = performSearch(sqlStatements)
        return rowMapperService.mapDataRowsToCandidates(rows, confidenceType, matchInput)
    }


    Candidate findRecord(String systemOfRecord, String identifier, Map matchInput) {
        def sql = sqlService.sqlInstance

        def systemOfRecordAttribute = matchConfig.matchAttributeConfigs.find { it.name == matchConfig.matchReference.systemOfRecordAttribute }
        def identifierAttribute = matchConfig.matchAttributeConfigs.find { it.name == matchConfig.matchReference.identifierAttribute }
        def row = sql.firstRow('SELECT * FROM ' + matchConfig.matchTable + ' WHERE ' + systemOfRecordAttribute.column + '=? AND ' + identifierAttribute.column + '=?',[systemOfRecord, identifier])

        return row ? rowMapperService.mapDataRowToCandidate(row, ConfidenceType.CANONICAL, matchInput) : null
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

