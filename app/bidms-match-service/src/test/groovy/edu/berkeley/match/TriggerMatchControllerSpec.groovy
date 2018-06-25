package edu.berkeley.match

import edu.berkeley.registry.model.SOR
import edu.berkeley.registry.model.SORObject
import spock.lang.Specification
import spock.lang.Unroll
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest

import javax.servlet.http.HttpServletResponse

class TriggerMatchControllerSpec extends Specification implements ControllerUnitTest<TriggerMatchController>, DataTest {

    void setupSpec() {
        mockDomains SORObject, SOR
    }

    def setup() {
        controller.newSORConsumerService = Mock(NewSORConsumerService)
        new SORObject(sor: new SOR(name: 'SIS').save(validate: false), sorPrimaryKey: '12345').save(validate: false, flush: true)
    }

    @Unroll
    void "test that expected path is chosen in controller, depending on input"() {
        given:
        Map attrMap = [systemOfRecord: 'SIS', sorPrimaryKey: sorPk, givenName: 'Kryf', surName: 'Plyf']
        request.json = attrMap

        when:
        controller.matchPerson(new SorKeyDataCommand(attrMap))

        then:
        serviceCallCount * controller.newSORConsumerService.matchPerson(_ as SORObject, attrMap, true)
        response.status == expectedStatus

        where:
        sorPk   | serviceCallCount | expectedStatus
        null    | 0                | HttpServletResponse.SC_BAD_REQUEST
        '54321' | 0                | HttpServletResponse.SC_BAD_REQUEST
        '12345' | 1                | HttpServletResponse.SC_OK
    }
}
