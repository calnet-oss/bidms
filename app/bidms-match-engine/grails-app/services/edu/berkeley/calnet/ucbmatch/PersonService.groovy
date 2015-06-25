package edu.berkeley.calnet.ucbmatch
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.response.ExactMatchResponse
import edu.berkeley.calnet.ucbmatch.response.Response
import grails.transaction.Transactional

@Transactional
class PersonService {
    MatchConfig matchConfig
    MatchService matchService

    Response matchPerson(Map matchInput) {
        def existingRecord = matchService.findExistingRecord(matchInput)
        if(existingRecord) {
            return new ExistingMatchResponse(responseData: existingRecord)
        }

        Set<Candidate> candidates = matchService.findCandidates(matchInput)
        if (!candidates) {
            log.debug("No match found for $matchInput")
            return Response.NOT_FOUND
        } else if (candidates.size() == 1 && candidates[0].exactMatch) {
            return new ExactMatchResponse(responseData: candidates[0])
        } else if(sameReferenceIdAndCanonical(candidates)) {
            return new ExactMatchResponse(responseData: candidates[0])
        }
        // Fall through is always a Fuzzy Match.
        return new FuzzyMatchResponse(responseData: candidates)
    }

    boolean sameReferenceIdAndCanonical(Set<Candidate> candidates) {
        return candidates.referenceId.unique().size() == 1 & candidates.every { it.exactMatch }
    }
}
