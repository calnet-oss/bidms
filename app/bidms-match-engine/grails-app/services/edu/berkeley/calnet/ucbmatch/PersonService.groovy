package edu.berkeley.calnet.ucbmatch
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.response.ExactMatchResponse
import edu.berkeley.calnet.ucbmatch.response.ExistingMatchResponse
import edu.berkeley.calnet.ucbmatch.response.FuzzyMatchResponse
import edu.berkeley.calnet.ucbmatch.response.Response
import grails.transaction.Transactional

@Transactional
class PersonService {
    MatchConfig matchConfig
    MatchService matchService
    /**
     * Will attempt to match a person
     * The input format could be like this:
     * [
     *      systemOfRecord: "ADVCON",
     *      identifier: '4000000',
     *      dateOfBirth: '1995-01-01',
     *      names: [
     *          [type: 'official', givenName: 'John', surName: 'Smith']
     *      ],
     *      identifiers: [
     *          [type: 'studentId', identifier: '60000000']
     *      ]
     * ]
     *
     * @param matchInput the format above
     * @return either an ExistingMatchResponse if the identifier is already in the registry
     *         A not found Response if no matches were found,
     *         an ExactMatchResponse if one and only one exact match was found
     *         or a FuzzyMatchResponse, if the match was not exact, but there were one or more fuzzymatches
     */
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
        return candidates.referenceId.unique().size() == 1 && candidates.every { it.exactMatch }
    }
}
