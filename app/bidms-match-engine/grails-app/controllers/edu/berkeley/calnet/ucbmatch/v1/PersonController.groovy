package edu.berkeley.calnet.ucbmatch.v1

import edu.berkeley.calnet.ucbmatch.command.SorAttributesCommand
import edu.berkeley.calnet.ucbmatch.response.Response
import grails.converters.JSON

import javax.servlet.http.HttpServletResponse

class PersonController {

    static namespace = "v1"

    def personService

    def getPerson(String systemOfRecord, String identifier, SorAttributesCommand command) {

        def result = personService.matchPerson(systemOfRecord, identifier, command.sorAttributes, true)
        renderResult(result)
    }

    def putPerson(String systemOfRecord, String identifier, SorAttributesCommand command) {
        def result = personService.matchPerson(systemOfRecord, identifier, command.sorAttributes)
        renderResult(result)
        HttpServletResponse.SC_CONFLICT
    }

    def deletePerson() {

    }


    private void renderResult(Response result) {
        if (result.record) {
            response.status = result.responseCode
            render(result.record as JSON)
        } else {
            response.status = result.responseCode
            render('')
        }
    }

}

