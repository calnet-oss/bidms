package edu.berkeley.match

import javax.servlet.http.HttpServletResponse

class TriggerMatchController {
    def newSORConsumerService
    def matchPerson(SorKeyDataCommand sorKeyDataCommand) {
        if(sorKeyDataCommand.hasErrors())  {
            render(status: HttpServletResponse.SC_BAD_REQUEST)
        } else {
            newSORConsumerService.matchPerson(sorKeyDataCommand.sorObject, sorKeyDataCommand.attributes)
            render(status: HttpServletResponse.SC_OK)
        }

    }
}
