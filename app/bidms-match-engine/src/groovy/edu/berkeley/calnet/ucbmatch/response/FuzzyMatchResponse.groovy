package edu.berkeley.calnet.ucbmatch.response

import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.response.Response

import javax.servlet.http.HttpServletResponse

class FuzzyMatchResponse extends Response {
    int responseCode = HttpServletResponse.SC_MULTIPLE_CHOICES
    Set<Candidate> responseData
    Map getJsonMap() {
        [
                partialMatchingRecords: responseData
        ]
    }
}
