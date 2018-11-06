package edu.berkeley.match

import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.SORObject
import grails.gorm.transactions.Transactional
import org.springframework.http.HttpStatus

@Transactional(rollbackFor = Exception)
class UidClientService {

    def grailsApplication
    def registryProvisioningRestBuilder

    /**
     * Makes a REST call to the Registry Provisioning to provision the given person
     * @param Person to provision
     * @throws RuntimeException if response status is not {@link HttpStatus#OK}
     */
    void provisionUid(Person person, boolean synchronousDownstream = true) {
        String endpoint = grailsApplication.config.rest.provisionUid.url
        // synchronousDownstream=true means synchronous downstream directory provisioning
        def response = registryProvisioningRestBuilder.post("$endpoint?uid=${person.uid}" + (synchronousDownstream ? "&synchronousDownstream=true" : "")) {
            accept 'application/json'
        }
        if (response.statusCode != HttpStatus.OK) {
            log.error("Error provisioning existing person ${person.uid}, response code: ${response.statusCode}:${response.text}")
        }
        log.debug "Successfully provisioned exising person ${person.uid}"
    }

    /**
     * Makes a REST call to the Registry Provisioning to assign new UID to person and provision
     * @param sorObject the SORObject to pass to Registry Provisioning
     * @throws RuntimeException if response status is not {@link HttpStatus#OK}
     */
    String provisionNewUid(SORObject sorObject, boolean synchronousDownstream = true) {
        String endpoint = grailsApplication.config.rest.provisionNewUid.url
        // synchronousDownstream=true means synchronous downstream directory provisioning
        def response = registryProvisioningRestBuilder.post("$endpoint?sorObjectId=${sorObject.id}" + (synchronousDownstream ? "&synchronousDownstream=true" : "")) {
            accept 'application/json'
        }
        if (response.statusCode != HttpStatus.OK) {
            log.error("Could not generate a new uid for sorObject ${sorObject.id}, response code: ${response.statusCode}:${response.text}")
        } else if (!response.json?.provisioningSuccessful) {
            if (response.json?.provisioningErrorMessage) {
                log.warn "Error provisioning new sorObject $sorObject.id for person ${response.json.uid}: ${response.json.provisioningErrorMessage}"
            } else {
                log.warn "Error provisioning new sorObject $sorObject.id: ${response.text}"
            }
        } else {
            log.debug "Successfully provisioned new sorObject $response.json.sorObjectId for person ${response.json.uid}"
            return response.json.uid
        }
        return null
    }
}
