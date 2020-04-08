package edu.berkeley.calnet.ucbmatch


import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.database.Record
import grails.gorm.transactions.Transactional

@Transactional
class MatchService {

    DatabaseService databaseService

    /**
     * Will search the database with for attributes in matchInput combined
     * with the matchConfig.
     */
    Set<Candidate> findCandidates(Map matchInput) {
        Set<Candidate> candidates = null
        log.debug("findCandidates (SuperCanonical) for ${matchInput.systemOfRecord}/${matchInput.identifier}")
        candidates = databaseService.searchDatabase(matchInput, ConfidenceType.SUPERCANONICAL)
        // Super canonical means if anything found, stop searching, as long
        // as there was exactly one candidate.
        // superCanonical rules in the match configuration should be
        // intentionally designed to only ever exactMatch one uid.
        // superCanonical rules are meant to match with definitive
        // identifiers, not match on demographic data.
        // Example: Data from a database SOR and data from a realtime
        // messaging SOR where it's really the same source system.  The
        // primary key is guaranteed to be the same in both.
        // If there's more than one "super candidate", or it's not an
        // exactMatch, log a prominent warning.  This should be considered a
        // match configuration problem, or a problem with incoming data.
        if ((candidates*.referenceId).unique().size() == 1 && candidates.every { it.exactMatch }) {
            // Matched with exactly one super candidate, stop processing any
            // further rules.
            log.debug("findCandidates found one super candidate for ${matchInput.systemOfRecord}/${matchInput.identifier}")
            return candidates
        } else if ((candidates*.referenceId).unique().size() == 1 && !candidates.every { it.exactMatch }) {
            // superCanonical rules should only ever produce exactMatches
            log.debug("One super candidate was found for ${matchInput.systemOfRecord}/${matchInput.identifier} but it was not an exactMatch.  This should not happen.  Check match configuration.  Super candidate referenceId: ${candidates.first().referenceId}")
            // Drop down to rest of rules since there's an errant super
            // candidate.
        } else if (candidates.size() > 1) {
            // Match configuration rules should be designed to prevent
            // multiple super candidates, so log this as a major warning.
            log.warn("More than one super candidate found (${candidates.size()}) for ${matchInput.systemOfRecord}/${matchInput.identifier}.  This should not happen.  Check match configuration.  Super candidate referenceIds: ${candidates*.referenceId}")
            // Drop down to rest of rules since there are errant multiple
            // super candidates.
        }
        log.debug("findCandidates (Canonical) for ${matchInput.systemOfRecord}/${matchInput.identifier}")
        candidates = databaseService.searchDatabase(matchInput, ConfidenceType.CANONICAL)
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
