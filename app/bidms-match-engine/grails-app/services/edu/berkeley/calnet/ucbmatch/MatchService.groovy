package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.database.Record
import grails.gorm.transactions.Transactional

@Transactional
class MatchService {

    DatabaseService databaseService

    /**
     * Will search the database with for attributes in matchInput combined with the matchConfig
     * @param matchInput
     * @return
     */
    Set<Candidate> findCandidates(Map matchInput) {
        log.debug("findCandidates (Canonical) for ${matchInput.systemOfRecord}/${matchInput.identifier}")
        Set<Candidate> candidates = databaseService.searchDatabase(matchInput, ConfidenceType.CANONICAL)
        //todo: if more than one candidate, then it's not canonical....
        if (!candidates) {
            log.debug("findCandidates (Potential) for ${matchInput.systemOfRecord}/${matchInput.identifier}")
            candidates = databaseService.searchDatabase(matchInput, ConfidenceType.POTENTIAL)
        }
        log.debug("findCandidates found ${candidates.size()} candidates")
        return candidates
    }

    Record findExistingRecord(Map matchInput) {
        return databaseService.findRecord(matchInput.systemOfRecord, matchInput.identifier as String)
    }

}
