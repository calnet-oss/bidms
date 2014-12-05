package edu.berkeley.calnet.ucbmatch.response

import edu.berkeley.calnet.ucbmatch.database.Record

import javax.servlet.http.HttpServletResponse

class Response {
    static NOT_FOUND = new Response(responseCode: HttpServletResponse.SC_NOT_FOUND)
    static CONFLICT = new Response(responseCode: HttpServletResponse.SC_CONFLICT)
    int responseCode
    Record record
}
