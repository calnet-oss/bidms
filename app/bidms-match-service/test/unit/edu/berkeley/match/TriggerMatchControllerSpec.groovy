package edu.berkeley.match

import edu.berkeley.registry.model.SOR
import edu.berkeley.registry.model.SORObject
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletResponse

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(TriggerMatchController)
@Mock([SORObject, SOR])
class TriggerMatchControllerSpec extends Specification {
    def setup() {
        controller.newSORConsumerService = Mock(NewSORConsumerService)
        new SORObject(sor: new SOR(name: 'SIS').save(validate: false), sorPrimaryKey: '12345').save(validate: false, flush: true)

    }

    @Unroll
    void "test that expected path is chosen in controller,depending on input"() {
        given:
        request.json = [ systemOfRecord: 'SIS', sorPrimaryKey: sorPk, givenName: 'Kryf', surName: 'Plyf']

        when:
        controller.matchPerson()

        then:
        serviceCallCount * controller.newSORConsumerService.matchPerson(_ as SORObject,[systemOfRecord: 'SIS', sorPrimaryKey: '12345', givenName: 'Kryf', surName: 'Plyf'])
        response.status == expectedStatus

        where:
        sorPk   | serviceCallCount | expectedStatus
        null    | 0                | HttpServletResponse.SC_BAD_REQUEST
        '54321' | 0                | HttpServletResponse.SC_BAD_REQUEST
        '12345' | 1                | HttpServletResponse.SC_OK
    }
}
