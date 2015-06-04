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
     * @Deprecated use provisionNewUid
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

    /**
     * Makes a REST call to the Registry Provisioning to assign new UID to person and provision
     * @param sorObject the SORObject to pass to Registry Provisioning
     */
    void provisionNewUid(SORObject sorObject) {
        String uidServiceUrl = grailsApplication.config.rest.uidService.url
        def response = restClient.post(uidServiceUrl) {
            accept 'application/json'
            json([id:sorObject.id])
        }
        if(!response.json.provisioningSuccessful) {
            log.warn "Error provisioning sorObject $response.json.sorObjectId for person ${response.json.uid}: ${response.json.provisioningErrorMessage}"
        }
    }
}
