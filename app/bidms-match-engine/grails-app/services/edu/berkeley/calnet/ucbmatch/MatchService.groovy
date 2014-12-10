package edu.berkeley.calnet.ucbmatch
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.database.InsertRecord
import edu.berkeley.calnet.ucbmatch.database.NullIdGenerator
import edu.berkeley.calnet.ucbmatch.database.UpdateRecord
import edu.berkeley.calnet.ucbmatch.exceptions.RecordExistsException
import groovy.sql.Sql
import org.joda.time.LocalDateTime

class MatchService {
    MatchConfig matchConfig

    static transactional = false

    @Delegate
    DatabaseService databaseService

    List<Candidate> findCandidates(String systemOfRecord, String identifier, Map sorAttributes) {
        List<Candidate> candidates = searchDatabase(systemOfRecord, identifier, sorAttributes, MatchType.CANONICAL)
        if(!candidates) {
            candidates = searchDatabase(systemOfRecord,identifier,sorAttributes,MatchType.POTENTIAL)
        }
        return candidates
    }

    InsertRecord insertCandidate(String systemOfRecord, String identifier, Map sorAttributes, String referenceId = null, boolean assignNewId = false) {
        def result = new InsertRecord()
        def idGenerator = getIdGenerator(assignNewId)

        def newRecordReferenceId = referenceId
        def requestTime = LocalDateTime.now()
        def resolutionTime = null
        withTransaction { Sql sql ->
            def existingRecord = findRecord(systemOfRecord, identifier)
            if(existingRecord) {
                if(existingRecord.referenceId) {
                    log.warn("Existing record found for $systemOfRecord/$identifier during insert.")
                    throw new RecordExistsException("Existing record found for $systemOfRecord/$identifier during insert.")
                } else {
                    log.debug("Existing unreconciled record found for $systemOfRecord/$identifier, replacing.")
                    removeRecord(systemOfRecord, identifier, sql)
                }
            }
        }



//        if(matchConfig.matchReference.responseType) {
//
//        }
        return new InsertRecord(referenceId: "1234", identifiers: [new Identifier(type: "Enterprise", identifier: "abcd")] )
    }

    UpdateRecord updateCandidate(String systemOfRecord, String identifier, Map sorAttributes) {
        null
    }



    private getIdGenerator(boolean assignNewId) {
        if(assignNewId && matchConfig.matchReference.idGenerator) {
            return matchConfig.matchReference.idGenerator
        } else {
            return NullIdGenerator
        }
    }


}
