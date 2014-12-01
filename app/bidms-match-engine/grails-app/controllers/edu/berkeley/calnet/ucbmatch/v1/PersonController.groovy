package edu.berkeley.calnet.ucbmatch.v1

import edu.berkeley.calnet.ucbmatch.command.SorAttributesCommand
import grails.converters.JSON

class PersonController {

    static namespace = "v1"

    def personService

    def getPerson(String systemOfRecord, String identifier, SorAttributesCommand command) {
        def result = personService.matchPerson(systemOfRecord, identifier, command.sorAttributes, true)
        if(response.hasProperty('result')) {
            response.status = result.responseCode
            render(response.result as JSON)
        } else {
            response.status = result.responseCode
            render('')
        }
    }

    def putPerson(String systemOfRecord, String identifier, SorAttributesCommand command) {
        def result = personService.matchPerson(systemOfRecord, identifier, command.sorAttributes)
        if(response.hasProperty('result')) {
            response.status = result.responseCode
            render(response.result as JSON)
        } else {
            response.status = result.responseCode
            render('')
        }

    }

    def delete() {

    }
}

