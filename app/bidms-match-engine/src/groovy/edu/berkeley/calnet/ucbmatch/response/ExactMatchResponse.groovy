package edu.berkeley.calnet.ucbmatch.response

import edu.berkeley.calnet.ucbmatch.database.Candidate

import javax.servlet.http.HttpServletResponse

class ExactMatchResponse extends Response {
    int responseCode = HttpServletResponse.SC_OK
    Candidate responseData
    Map getJsonMap() {
        [
                matchingRecord: responseData
        ]
    }
}
