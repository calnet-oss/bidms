package edu.berkeley.calnet.ucbmatch.response

import edu.berkeley.calnet.ucbmatch.database.Record
import edu.berkeley.calnet.ucbmatch.response.Response

import javax.servlet.http.HttpServletResponse

class ExistingMatchResponse extends Response {
    String systemOfRecord
    String identifier
    int responseCode = HttpServletResponse.SC_FOUND
    Record responseData
    Map getJsonMap() {
        [
                matchingRecord: responseData
        ]
    }
}
