package edu.berkeley.match

import grails.transaction.Transactional
import org.springframework.http.HttpStatus

@Transactional
class MatchClientService {
    def restClient
    def grailsApplication

    MatchResponse match(Map<String, String> p) {
        String matchUrl = grailsApplication.config.match.ucbMatchUrl
        //[systemOfRecord: 'SIS', sorIdentifier: 'SIS00001', givenName: 'firstName', familyName: 'lastName', dateOfBirth: 'DOB', socialSecurityNumber: 'SSN']
        def jsonMap = buildJsonMap(p)
        def response = restClient.post(matchUrl) {
            accept 'application/json'
            json(jsonMap)
        }
        switch(response.statusCode) {
            case HttpStatus.NOT_FOUND:
                return new NoMatch()
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

    private ExactMatch exactMatch(def json) {
        new ExactMatch(uid: '') // TODO: Extract UID from json
    }

    private PartialMatch partialMatch(def json) {
        new PartialMatch(uids: []) // TODO: Extract UIDS from json
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
