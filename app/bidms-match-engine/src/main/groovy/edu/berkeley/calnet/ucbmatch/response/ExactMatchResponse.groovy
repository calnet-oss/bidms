package edu.berkeley.calnet.ucbmatch.response

import edu.berkeley.calnet.ucbmatch.database.Record

import javax.servlet.http.HttpServletResponse

class ExactMatchResponse extends Response {
    int responseCode = HttpServletResponse.SC_OK
    Record responseData
    Map getJsonMap() {
        [
                matchingRecord: responseData
        ]
    }
}
