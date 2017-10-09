package edu.berkeley.match

import grails.converters.JSON

import javax.servlet.http.HttpServletResponse

class TriggerMatchController {
    def newSORConsumerService

    def matchPerson(SorKeyDataCommand sorKeyDataCommand) {
        def hasErrors = !sorKeyDataCommand.validate()
        if (hasErrors) {
            log.debug("could not trigger a match: $sorKeyDataCommand.errors")
            render(status: HttpServletResponse.SC_BAD_REQUEST)
        } else {
            log.debug("Sor Key Data attributes. $sorKeyDataCommand.attributes")
            Map<String,String> result = newSORConsumerService.matchPerson(sorKeyDataCommand.sorObject, sorKeyDataCommand.attributes, sorKeyDataCommand.synchronousDownstream)
            if(result) {
                render result as JSON
            }
            else {
                render(status: HttpServletResponse.SC_OK)
            }
        }
    }
}
