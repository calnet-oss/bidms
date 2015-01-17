package edu.berkeley.match

import edu.berkeley.registry.model.PartialMatch
import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.SORObject
import grails.transaction.Transactional

@Transactional
class DatabaseService {

    void assignUidToSOR(SORObject sorObject, Person person) {
        sorObject.person = person
        sorObject.save(failOnError: true)
    }

    void storePartialMatch(SORObject sorObject, List<Person> matchingPeople) {
        removeExistingPartialMatches(sorObject)
        matchingPeople.each {
            createPartialMatch(sorObject, it)
        }
    }

    private static void createPartialMatch(SORObject sorObject, Person person) {
        def partialMatch = new PartialMatch(sorObject: sorObject, person: person)
        partialMatch.save(failOnError: true)
    }

    private static void removeExistingPartialMatches(SORObject sorObject) {
        def partialMatch = PartialMatch.where { sorObject == sorObject }
        partialMatch.deleteAll()
    }
}
