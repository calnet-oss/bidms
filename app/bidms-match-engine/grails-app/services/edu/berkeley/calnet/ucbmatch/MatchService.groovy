package edu.berkeley.calnet.ucbmatch
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.database.Record
import grails.transaction.Transactional

@Transactional
class MatchService {
    MatchConfig matchConfig

    DatabaseService databaseService

    Set<Candidate> findCandidates(Map matchInput) {
        Set<Candidate> candidates = databaseService.searchDatabase(matchInput, ConfidenceType.CANONICAL)
        //todo: if more than one candidate, then it's not canonical....
        if (!candidates) {
            candidates = databaseService.searchDatabase(matchInput, ConfidenceType.POTENTIAL)
        }
        return candidates
    }

    Record findExistingRecord(Map matchInput) {
        return databaseService.findRecord(matchInput.systemOfRecord, matchInput.identifier, matchInput)
    }

}
