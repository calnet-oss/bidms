package edu.berkeley.match

import edu.berkeley.registry.model.Person
import grails.transaction.Transactional

@Transactional
class DownstreamJMSService {
    def jmsService
    def grailsApplication

    /**
     * Notify downstream systems (The registry) that a Person is ready to (re)provision
     * @param person
     */
    def provision(Person person) {
        def provisionUidQueueName = grailsApplication.config.jms.provisioning.provisionUID.queueName
        jmsService.send(provisionUidQueueName, [uid: person.uid])
    }
}
