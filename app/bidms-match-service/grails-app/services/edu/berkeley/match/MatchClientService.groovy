package edu.berkeley.match

import edu.berkeley.registry.model.Person
import grails.transaction.Transactional
import org.springframework.http.HttpStatus

@Transactional(readOnly = true)
class MatchClientService {
    def restClient
    def grailsApplication

    PersonMatch match(Map<String, String> p) {
        String matchUrl = grailsApplication.config.match.ucbMatchUrl
        //[systemOfRecord: 'SIS', sorIdentifier: 'SIS00001', givenName: 'firstName', familyName: 'lastName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN']
        def jsonMap = buildJsonMap(p)
        def response = restClient.post(matchUrl) {
            accept 'application/json'
            json(jsonMap)
        }
        switch (response.statusCode) {
            case HttpStatus.NOT_FOUND:
                return new PersonNoMatch()
            case HttpStatus.OK:
                return exactMatch(response.json)
            case HttpStatus.MULTIPLE_CHOICES:
                return partialMatch(response.json)
            default:
                log.error("Got wrong return code from match engine..")
                // TODO: Determin what to do in this situation
                throw new RuntimeException("Got wrong return code from match engine: $response.statusCode.reasonPhrase ($response.statusCode)")
        }

    }

    private static PersonExactMatch exactMatch(def json) {
        // Person object is not to be changed
        def person = Person.read(json.matchingRecord.referenceId as String)
        new PersonExactMatch(person: person)
    }

    private static PersonPartialMatches partialMatch(def json) {
        def people = json.partialMatchingRecords*.referenceId.collect {
            // Person object is not to be changed
            Person.read(it as String)
        }
        new PersonPartialMatches(people: people)
    }


    Map buildJsonMap(Map<String, String> p) {
        def map = [systemOfRecord: p.systemOfRecord, identifier: p.sorIdentifier]
        if (p.dateOfBirth) {
            map.dateOfBirth = p.dateOfBirth
        }

        if (p.familyName || p.givenName) {
            def name = [type: "official"]
            if (p.givenName) {
                name.given = p.givenName
            }
            if (p.familyName) {
                name.family = p.familyName
            }
            map.names = [name]
        }
        if (p.socialSecurityNumber) {
            map.identifiers = [
                    [type: "national", identifier: p.socialSecurityNumber]
            ]
        }
        return map
    }
}
