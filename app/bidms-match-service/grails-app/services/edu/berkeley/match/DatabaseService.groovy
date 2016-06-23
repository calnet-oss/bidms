package edu.berkeley.match

import edu.berkeley.registry.model.PartialMatch
import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.SORObject
import grails.transaction.Transactional

@Transactional
class DatabaseService {

    /**
     * Assign a person to a SORObject, linking the two together.
     * @param sorObject
     * @param person
     */
    void assignUidToSOR(SORObject sorObject, Person person) {
        sorObject.person = person
        // clear sorObject out of PartialMatch table if it's there
        removeExistingPartialMatches(sorObject)
        sorObject.save(failOnError: true)
    }

    /**
     * Store a potential match(es), linking a sorObject to the People in the Database
     * @param sorObject
     * @param matchingPeople
     */
    void storePartialMatch(SORObject sorObject, List<Person> matchingPeople) {
        // transaction ensures that the deletes are flushed before we try to reinsert
        PartialMatch.withTransaction {
            removeExistingPartialMatches(sorObject)
        }
        matchingPeople.each {
            createPartialMatch(sorObject, it)
        }
    }

    private void createPartialMatch(SORObject sorObject, Person person) {
        def partialMatch = PartialMatch.findOrCreateWhere(sorObject: sorObject, person: person)
        try {
            partialMatch.save(failOnError: true)
        } catch (e) {
            log.error("Failed to save PartialMatch for SORObject: ${sorObject}, Person: ${person}", e)
        }
    }

    private static void removeExistingPartialMatches(SORObject sorObject) {
        List partialMatches = PartialMatch.findAllBySorObject(sorObject)
        PartialMatch.deleteAll(partialMatches)
    }
}
