package edu.berkeley.match

import edu.berkeley.registry.model.Person
import grails.transaction.Transactional
import org.springframework.http.HttpStatus

@Transactional(readOnly = true)
class MatchClientService {
    def restClient
    def grailsApplication

    /**
     * Call the match-engine to see if the database has a match on an existing record.
     * The match-engine configuration determins if there is no match, a single (canonical) match, or a partial match
     *
     * @param a map containing the some or all of the following properties (in this format)
     * [
     *      systemOfRecord: 'SIS', sorPrimaryKey: 'SIS00001', fullName: 'lastName, firstName middleName', givenName: 'firstName', middleName: 'middleName', lastName: 'lastName',
     *      dateOfBirth: 'DOB', email: 'some@email.com', socialSecurityNumber: 'SSN', otherIds: [studentId: 'abc', employeeId: 'xyz']
     * ]
     * @return
     * @throws RuntimeException a runtime exception if the match-engine returns other status codes than NOT_FOUND, OK or MULTIPLE_CHOICES
     */
    PersonMatch match(Map<String, String> p) {
        String matchUrl = grailsApplication.config.match.ucbMatchEngineUrl
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
        def person = Person.findByUid(json.existingRecord.referenceId as String)
        new PersonExactMatch(person: person)
    }

    private static PersonPartialMatches partialMatch(def json) {
        def people = json.partialMatchingRecords*.referenceId.collect {
            // Person object is not to be changed
            Person.findByUid(it as String)
        }
        new PersonPartialMatches(people: people)
    }

    /**
     * Map input parameters to a Match-Engine request
     * @param params
     * @return
     */
    private static Map buildJsonMap(Map<String, String> params) {
        def map = [systemOfRecord: params.systemOfRecord, identifier: params.sorPrimaryKey]

        // Copy top level properties
        ['dateOfBirth', 'email'].each {
            if (params[it]) {
                map[it] = params[it]
            }
        }

        // Copy name attributes to names structure
        def name = ['givenName', 'middleName', 'surName', 'fullName'].collectEntries {
            [it, params[it]]
        }.findAll { it.value }

        if (name) {
            name.type = "official"
            map.names = [name]
        }

        if (params.socialSecurityNumber || params.otherIds) {

            // Copy other identifiers (comes in a map) to the identifiers list
            map.identifiers = params.otherIds?.collect { type, value ->
                [
                        type      : type,
                        identifier: value
                ]
            } ?: []

            // Finally add socialSecurityNumber if present
            if (params.socialSecurityNumber) {
                map.identifiers << [type: "national", identifier: params.socialSecurityNumber]
            }
        }
        return map
    }
}
