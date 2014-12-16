package edu.berkeley.calnet.ucbmatch.response

import javax.servlet.http.HttpServletResponse

class ExactMatchResponse extends Response {
    int responseCode = HttpServletResponse.SC_OK
    def responseData
}
