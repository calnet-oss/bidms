package edu.berkeley.match

import grails.transaction.Transactional
import grails.util.Holders

@Transactional
class NewSORConsumerService {
    static exposes = ["jms"]

    static destination = Holders.config.jms.newSORQueue
    static isTopic = false
    static adapter = "transacted"
    static container = "transacted"

    def matchClientService
    def uidClientService
    def databaseService
    def downstreamJMSService

    def onMessage(msg) {

        return null
    }
}
