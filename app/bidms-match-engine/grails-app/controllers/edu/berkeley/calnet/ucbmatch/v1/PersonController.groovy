package edu.berkeley.calnet.ucbmatch.v1
import edu.berkeley.calnet.ucbmatch.command.MatchCommand
import grails.converters.JSON

class PersonController {

    static namespace = "v1"

    def personService

    def getPerson(MatchCommand command) {
        def result = personService.matchPerson(command.properties['systemOfRecord','identifier','sorAttributes'])
        if (result.responseData) {
            response.status = result.responseCode
            render(result.responseData as JSON)
        } else {
            response.status = result.responseCode
            render('')
        }
    }


}

