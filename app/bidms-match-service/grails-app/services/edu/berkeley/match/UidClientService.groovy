package edu.berkeley.match
import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.SORObject
import grails.transaction.Transactional
import org.springframework.http.HttpStatus

@Transactional
class UidClientService {

    def restClient
    def grailsApplication
    /**
     * Makes a REST call to the Registry Provisioning to provision the given person
     * @param Person to provision
     * @throws RuntimeException if response status is not {@link HttpStatus#OK}
     */
    void provisionUid(Person person) {
        String endpoint = grailsApplication.config.rest.provisionUid.url
        def response = restClient.post("$endpoint?uid=${person.uid}") {
            accept 'application/json'
        }
        if(response.statusCode != HttpStatus.OK) {
            log.error("Error provisioning person ${person.uid}, response code: ${response.statusCode}:${response.text}")
        }
    }

    /**
     * Makes a REST call to the Registry Provisioning to assign new UID to person and provision
     * @param sorObject the SORObject to pass to Registry Provisioning
     * @throws RuntimeException if response status is not {@link HttpStatus#OK}
     */
    void provisionNewUid(SORObject sorObject) {
        String endpoint = grailsApplication.config.rest.provisionNewUid.url
        def response = restClient.post("$endpoint?sorObjectId=${sorObject.id}") {
            accept 'application/json'
        }
        if(response.statusCode != HttpStatus.OK) {
            log.error("Could not generate a new uid for sorObject ${sorObject.id}, response code: ${response.statusCode}:${response.text}")
        }
        if(!response.json.provisioningSuccessful) {
            log.warn "Error provisioning sorObject $response.json.sorObjectId for person ${response.json.uid}: ${response.json.provisioningErrorMessage}"
        }
    }
}
