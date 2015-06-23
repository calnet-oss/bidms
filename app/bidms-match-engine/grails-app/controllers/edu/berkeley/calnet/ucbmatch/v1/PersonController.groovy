package edu.berkeley.calnet.ucbmatch.v1

import grails.converters.JSON
import org.springframework.http.HttpStatus

class PersonController {

    static namespace = "v1"

    def personService

    def getPerson() {
        try {
            def result = personService.matchPerson(request.JSON)
            if (result.hasProperty('jsonMap')) {
                response.status = result.responseCode
                render(result.jsonMap as JSON)
            } else {
                response.status = result.responseCode
                render('')
            }
        } catch(Exception ex) {
            render(status: HttpStatus.INTERNAL_SERVER_ERROR, contentType: "application/json") {
                text = ex.message
            }
        }
    }


}

