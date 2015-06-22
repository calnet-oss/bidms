package edu.berkeley.calnet.ucbmatch.v1

import grails.converters.JSON
import groovy.json.JsonSlurper

class PersonController {

    static namespace = "v1"

    def personService

    def getPerson() {
        if(!request.parameterMap.json?.size()) {
            throw new RuntimeException("json is a required parameter")
        }
        def result = personService.matchPerson(new JsonSlurper().parseText(request.parameterMap.json[0]))
        if (result.hasProperty('jsonMap')) {
            response.status = result.responseCode
            render(result.jsonMap as JSON)
        } else {
            response.status = result.responseCode
            render('')
        }
    }


}

