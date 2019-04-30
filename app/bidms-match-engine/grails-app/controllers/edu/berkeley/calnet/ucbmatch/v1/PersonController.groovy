package edu.berkeley.calnet.ucbmatch.v1

import edu.berkeley.calnet.ucbmatch.PersonService
import edu.berkeley.calnet.ucbmatch.response.Response
import grails.converters.JSON
import org.grails.web.json.JSONObject
import org.springframework.http.HttpStatus

class PersonController {

    static namespace = "v1"

    PersonService personService

    def getPerson() {
        try {
            Response result = personService.matchPerson(request.JSON)
            if (result.hasProperty('jsonMap') && result.jsonMap) {
                log.debug "Match found with results: ${result.jsonMap as JSON}"
                response.status = result.responseCode
                render(result.jsonMap as JSON)
            } else {
                log.info "No Match found with params: ${personService.getRedactedParams((JSONObject) request.JSON)}"
                render(status: result.responseCode, contentType: "application/json") {
                    text: "not found"
                }
            }
        } catch (Exception ex) {
            log.error("Exception", ex)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR, contentType: "application/json") {
                text:
                ex.message
            }
        }
    }
}

