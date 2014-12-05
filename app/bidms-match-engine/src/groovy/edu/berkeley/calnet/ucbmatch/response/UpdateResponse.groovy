package edu.berkeley.calnet.ucbmatch.response

import edu.berkeley.calnet.ucbmatch.database.UpdateRecord

import javax.servlet.http.HttpServletResponse

class UpdateResponse extends Response {
    int responseCode = HttpServletResponse.SC_OK
    UpdateRecord record
}
