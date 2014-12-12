package edu.berkeley.calnet.ucbmatch.response

import javax.servlet.http.HttpServletResponse

class Response {
    static NOT_FOUND = new Response(responseCode: HttpServletResponse.SC_NOT_FOUND)
    static CONFLICT = new Response(responseCode: HttpServletResponse.SC_CONFLICT)
    int responseCode
    def responseData
}
