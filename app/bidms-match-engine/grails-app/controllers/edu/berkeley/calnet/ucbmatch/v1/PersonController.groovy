package edu.berkeley.calnet.ucbmatch.v1

import grails.converters.JSON

class PersonController {

    static namespace = "v1"

    def personService

    def getPerson() {
        def result = personService.matchPerson(request.JSON)
        if (result.responseData) {
            response.status = result.responseCode
            render(result.responseData as JSON)
        } else {
            response.status = result.responseCode
            render('')
        }
    }


}

