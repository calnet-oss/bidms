package edu.berkeley.match

import edu.berkeley.registry.model.PartialMatch
import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.SORObject
import grails.gorm.transactions.Transactional

@Transactional(rollbackFor = Exception)
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
        sorObject.save(failOnError: true, flush: true)
    }

    /**
     * Store a potential match(es), linking a sorObject to the People in the Database
     * @param sorObject
     * @param matchingPeople
     */
    void storePartialMatch(SORObject sorObject, List<PersonPartialMatch> matchingPeople) {
        removeExistingPartialMatches(sorObject)
        matchingPeople.each {
            createPartialMatch(sorObject, it)
        }
    }

    @Transactional(rollbackFor = Exception)
    private void createPartialMatch(SORObject sorObject, PersonPartialMatch personPartialMatch) {
        PartialMatch partialMatch = PartialMatch.findOrCreateWhere(sorObject: sorObject, person: personPartialMatch.person)

        try {
            partialMatch.metaData.ruleNames = personPartialMatch.ruleNames
            partialMatch.save(flush: true, failOnError: true)
        } catch (e) {
            log.error("Failed to save PartialMatch for SORObject: ${sorObject}, Person: ${personPartialMatch}", e)
        }
    }

    @Transactional(rollbackFor = Exception)
    private void removeExistingPartialMatches(SORObject sorObject) {
        List<PartialMatch> partialMatches = PartialMatch.findAllBySorObject(sorObject)
        partialMatches.each {
            it.delete(flush: true, failOnError: true)
        }
    }
}
