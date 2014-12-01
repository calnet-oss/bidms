package edu.berkeley.calnet.ucbmatch
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Match
import edu.berkeley.calnet.ucbmatch.response.InsertResponse
import edu.berkeley.calnet.ucbmatch.response.Response
import grails.transaction.Transactional

@Transactional
class PersonService {
    MatchConfig matchConfig
    MatchService matchService

    Response matchPerson(String systemOfRecord, String identifier, Map sorAttributes, boolean readOnly = false) {
        List<Match> matches = matchService.findCandidates(systemOfRecord, identifier, sorAttributes)
        if(!matches) {
            return readOnly ? Response.NOT_FOUND : insertNewRecord(systemOfRecord, identifier, sorAttributes)
        } else {
            if(matches.size() == 1) {
                if(matches[0].confidence >= 90) {
                    return insertOrUpdateRecord(systemOfRecord, identifier, sorAttributes)
                }
            }
        }
    }



    Response insertNewRecord(String systemOfRecord, String identifier, Map sorAttributes) {
        def insertResult = matchService.insertCandidate(systemOfRecord, identifier, sorAttributes)
        return new InsertResponse(result: insertResult)
    }

    Response insertOrUpdateRecord(String systemOfRecord, String identifier, Map sorAttributes) {
        null
    }

}
