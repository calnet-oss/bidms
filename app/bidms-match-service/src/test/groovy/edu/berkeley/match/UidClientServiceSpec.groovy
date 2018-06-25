package edu.berkeley.match

import edu.berkeley.match.testutils.TimeoutResponseCreator
import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.SORObject
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.plugins.rest.client.RestBuilder
import grails.testing.services.ServiceUnitTest
import grails.web.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.client.ResourceAccessException
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@Build([Person, SORObject])
class UidClientServiceSpec extends Specification implements ServiceUnitTest<UidClientService>, BuildDataTest {
    private static final PROVISION_ENDPOINT = 'http://localhost/registry-provisioning/provision/save'

    def setup() {
        grailsApplication.config.rest = [provisionUid: [url: PROVISION_ENDPOINT], provisionNewUid: [url: PROVISION_ENDPOINT]]
        service.restClient = new RestBuilder()
    }

    @Unroll
    void "provision a new uid and #description and return a success"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        SORObject sorObject = SORObject.build()
        mockServer.expect(requestTo("$PROVISION_ENDPOINT?sorObjectId=${sorObject.id}" + (synchronousDownstream ? "&synchronousDownstream=true" : "")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(""))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withSuccess("{'provisioningSuccessful': 'true'}", MediaType.APPLICATION_JSON))

        when:
        service.provisionNewUid(sorObject, synchronousDownstream)

        then:
        mockServer.verify()

        where:
        description                           | synchronousDownstream
        "provision downstream synchronously"  | true
        "provision downstream asynchronously" | false
    }

    void "provisioning a new uid server times out and exception is thrown"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        SORObject sorObject = SORObject.build()
        mockServer.expect(requestTo("$PROVISION_ENDPOINT?sorObjectId=${sorObject.id}&synchronousDownstream=true"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(""))
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
        mockServer.expect(requestTo("$PROVISION_ENDPOINT?uid=${person.uid}&synchronousDownstream=true"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(""))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(withSuccess())

        when:
        service.provisionUid(person)

        then:
        mockServer.verify()
    }

    void "provisioning an existing uid server times out and exception is thrown"() {
        setup:
        final mockServer = MockRestServiceServer.createServer(service.restClient.restTemplate)
        Person person = Person.build(uid: "1")
        mockServer.expect(requestTo("$PROVISION_ENDPOINT?uid=${person.uid}&synchronousDownstream=true"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(""))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json"))
                .andRespond(TimeoutResponseCreator.withTimeout())

        when:
        service.provisionUid(person)

        then:
        thrown(ResourceAccessException)
    }
}
