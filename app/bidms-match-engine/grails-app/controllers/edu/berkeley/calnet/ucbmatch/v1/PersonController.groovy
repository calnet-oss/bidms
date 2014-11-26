package edu.berkeley.calnet.ucbmatch.v1

import grails.converters.JSON

class PersonController {

    static namespace = "v1"

    def personService

    def index() {
        render personService.serviceMethod() as JSON
    }
}
