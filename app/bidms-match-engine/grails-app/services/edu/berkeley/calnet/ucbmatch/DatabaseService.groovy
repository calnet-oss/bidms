package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.database.Record
import grails.transaction.Transactional
import groovy.sql.Sql

@Transactional
class DatabaseService {
    def sqlService

    List<Candidate> searchDatabase(String systemOfRecord, String identifier, Map sorAttributes, MatchType matchType) {
        []
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

