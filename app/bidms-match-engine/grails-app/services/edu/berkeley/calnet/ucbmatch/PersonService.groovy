package edu.berkeley.calnet.ucbmatch
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.response.InsertResponse
import edu.berkeley.calnet.ucbmatch.response.Response
import edu.berkeley.calnet.ucbmatch.response.UpdateResponse

class PersonService {
    static transactional = false
    MatchConfig matchConfig
    MatchService matchService

    Response matchPerson(String systemOfRecord, String identifier, Map sorAttributes, boolean readOnly = false) {

        List<Candidate> candidates = matchService.findCandidates(systemOfRecord, identifier, sorAttributes)
        if(!candidates) {
            log.debug("")
            return readOnly ? Response.NOT_FOUND : insertNewRecord(systemOfRecord, identifier, sorAttributes)
        } else {
            if(candidates.size() == 1) {
                if(candidates[0].confidence >= 90) {
                    if(isExistingCandidateIdentifiersExists(systemOfRecord, identifier, candidates[0])) {
                        log.debug("Promoted insert to update due to existing record for $systemOfRecord/$identifier")
                        return updateExistingRecord(systemOfRecord, identifier, sorAttributes)
                    } else {
                        return insertNewRecord(systemOfRecord, identifier, sorAttributes, candidates[0].referenceId)
                    }
                }
            }
        }
        log.debug("No exact match found, resorting to fuzzy match ")
        if(candidates.any { isExistingCandidateIdentifiersExists(systemOfRecord, identifier, it)}) {
            log.debug("Converted fuzzy match to conflict due to existing match for $systemOfRecord/$identifier")
            return Response.CONFLICT
        }
        if(readOnly) {

        }
    }



    Response insertNewRecord(String systemOfRecord, String identifier, Map sorAttributes, String referenceId = null) {
        def insertResult = matchService.insertCandidate(systemOfRecord, identifier, sorAttributes)
        return new InsertResponse(record: insertResult)
    }

    Response updateExistingRecord(String systemOfRecord, String identifier, Map sorAttributes) {
        def updateResult = matchService.updateCandidate(systemOfRecord, identifier, sorAttributes)
        return new UpdateResponse(record: updateResult)
    }

    boolean isExistingCandidateIdentifiersExists(String systemOfRecord, String identifier, Candidate candidate) {
        return candidate?.identifiers?.any { it.type == systemOfRecord && it.identifier == identifier}
    }

}
