package edu.berkeley.calnet.ucbmatch.v1
import edu.berkeley.calnet.ucbmatch.command.SorAttributesCommand
import edu.berkeley.calnet.ucbmatch.response.Response
import grails.converters.JSON

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
    }

    def deletePerson() {

    }


    private void renderResult(Response result) {
        if (result.responseData) {
            response.status = result.responseCode
            render(result.responseData as JSON)
        } else {
            response.status = result.responseCode
            render('')
        }
    }

}

