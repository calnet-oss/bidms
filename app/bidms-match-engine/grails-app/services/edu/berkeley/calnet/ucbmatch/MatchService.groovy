package edu.berkeley.calnet.ucbmatch
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.database.NullIdGenerator
import grails.transaction.Transactional

@Transactional
class MatchService {
    MatchConfig matchConfig

    DatabaseService databaseService

    Set<Candidate> findCandidates(Map matchInput) {
        Set<Candidate> candidates = databaseService.searchDatabase(matchInput, ConfidenceType.CANONICAL)
        if (!candidates) {
            candidates = databaseService.searchDatabase(matchInput, ConfidenceType.POTENTIAL)
        }
        return candidates
    }

    Candidate findExistingRecord(Map matchInput) {
        return databaseService.findRecord(matchInput.systemOfRecord, matchInput.identifier)
    }

    private getIdGenerator(boolean assignNewId) {
        if (assignNewId && matchConfig.matchReference.idGenerator) {
            return matchConfig.matchReference.idGenerator
        } else {
            return NullIdGenerator
        }
    }


}
