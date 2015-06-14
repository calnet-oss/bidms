package edu.berkeley.match

import edu.berkeley.match.testutils.TimeoutResponseCreator
import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.SORObject
import grails.buildtestdata.mixin.Build
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.servlet.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.ResourceAccessException
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(UidClientService)
@Build([Person, SORObject])
class UidClientServiceSpec extends Specification {
    private static final PROVISION_ENDPOINT = 'http://localhost/registry-provisioning/provision/save'

    def setup() {
        grailsApplication.config.rest = [provisionUid: [url: PROVISION_ENDPOINT], provisionNewUid: [url: PROVISION_ENDPOINT]]
        service.restClient = new RestBuilder()
    }

    void "provision a new uid and return a success"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        SORObject sorObject = SORObject.build()
        mockServer.expect(requestTo(PROVISION_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("""{"id":${sorObject.id}}"""))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withSuccess("{'provisioningSuccessful': 'true'}", MediaType.APPLICATION_JSON))

        when:
        service.provisionNewUid(sorObject)

        then:
        mockServer.verify()
    }

    void "provisioning a new uid throws an exception"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        SORObject sorObject = SORObject.build()
        mockServer.expect(requestTo(PROVISION_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("""{"id":${sorObject.id}}"""))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withNoContent())

        when:
        service.provisionNewUid(sorObject)

        then:
        thrown(RuntimeException)
    }

    void "provisioning a new uid server times out and exception is thrown"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        SORObject sorObject = SORObject.build()
        mockServer.expect(requestTo(PROVISION_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("""{"id":${sorObject.id}}"""))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(TimeoutResponseCreator.withTimeout())

        when:
        service.provisionNewUid(sorObject)

        then:
        thrown(ResourceAccessException)
    }

    void "provision an existing uid and return a success"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        Person person = Person.build(uid: "1")
        mockServer.expect(requestTo(PROVISION_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("""{"uid":"${person.uid}"}"""))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withSuccess())

        when:
        service.provisionUid(person)

        then:
        mockServer.verify()
    }

    void "provisioning an existing uid throws an exception"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        Person person = Person.build(uid: "1")
        mockServer.expect(requestTo(PROVISION_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("""{"uid":"${person.uid}"}"""))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withNoContent())

        when:
        service.provisionUid(person)

        then:
        thrown(RuntimeException)
    }

    void "provisioning an existing uid server times out and exception is thrown"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        Person person = Person.build(uid: "1")
        mockServer.expect(requestTo(PROVISION_ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("""{"uid":"${person.uid}"}"""))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(TimeoutResponseCreator.withTimeout())

        when:
        service.provisionUid(person)

        then:
        thrown(ResourceAccessException)
    }
}
