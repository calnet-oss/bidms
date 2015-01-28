package edu.berkeley.match
import edu.berkeley.registry.model.Person
import grails.transaction.Transactional
import org.springframework.http.HttpStatus

@Transactional
class UidClientService {

    def restClient
    def grailsApplication
    /**
     * Makes a REST call to the UID Service. The UID service assigns a new UID and creates a minimal Person in the registry
     * @param sorAttributes
     * @return Person created by the UID Service in the registry
     */
    Person createUidForPerson(Map sorAttributes) {
        String uidServiceUrl = grailsApplication.config.rest.uidService.url
        def response = restClient.post(uidServiceUrl) {
            accept 'application/json'
            json(sorAttributes)
        }
        if(response.statusCode == HttpStatus.OK) {
            def uid = response.json.uid
            return Person.findByUid(uid)
        } else {
            throw new RuntimeException("Could not generate new UID for $sorAttributes")
        }
    }
}
