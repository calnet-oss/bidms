package edu.berkeley.match
import edu.berkeley.registry.model.Person
import grails.transaction.Transactional
import org.springframework.http.HttpStatus

@Transactional
class UidClientService {

    def restClient
    def grailsApplication

    Person createUidForPerson(Map sorAttributes) {
        String uidServiceUrl = grailsApplication.config.match.uidServiceUrl
        def response = restClient.post(uidServiceUrl) {
            accept 'application/json'
            json(sorAttributes)
        }
        if(response.statusCode == HttpStatus.OK) {
            def uid = response.json.uid
            return Person.get(uid)
        } else {
            throw new RuntimeException("Could not generate new UID for $sorAttributes")
        }
    }
}
