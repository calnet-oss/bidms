package edu.berkeley.calnet.ucbmatch.v1

import grails.converters.JSON

class PersonController {

    static namespace = "v1"

    def personService

    def getPerson() {
        def result = personService.matchPerson(request.parameterMap)
        if (result.hasProperty('jsonMap')) {
            response.status = result.responseCode
            render(result.jsonMap as JSON)
        } else {
            response.status = result.responseCode
            render('')
        }
    }


}

