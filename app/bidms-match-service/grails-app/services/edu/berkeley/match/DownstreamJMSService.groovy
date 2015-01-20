package edu.berkeley.match

import edu.berkeley.registry.model.Person
import grails.transaction.Transactional

@Transactional
class DownstreamJMSService {

    /**
     * Notify downstream systems (The registry) that a Person is ready to (re)provision
     * @param person
     */
    def provision(Person person) {
        // Send a JMS message on a queue
    }
}
