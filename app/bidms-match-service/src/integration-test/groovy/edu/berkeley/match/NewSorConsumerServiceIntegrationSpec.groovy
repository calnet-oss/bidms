package edu.berkeley.match

import edu.berkeley.registry.model.PartialMatch
import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.SOR
import edu.berkeley.registry.model.SORObject
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.transaction.Transactional
import groovy.transform.Synchronized
import groovy.util.logging.Slf4j
import org.apache.activemq.command.ActiveMQMapMessage
import org.springframework.http.HttpStatus
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
@Integration
class NewSorConsumerServiceIntegrationSpec extends Specification {
    @Shared
    def grailsApplication

    static transactional = false

    def newSORConsumerService
    def databaseService

    @Shared
    VertxServer matchEngine = new VertxServer(host: 'localhost', port: 8089)
    @Shared
    VertxServer uidService = new VertxServer(host: 'localhost', port: 8084)

    @Synchronized
    @Transactional
    def setup() {
        grailsApplication.config.rest.matchEngine.url = 'http://localhost:8089/ucb-match/v1/person'  // Use local mock server
        grailsApplication.config.rest.provisionNewUid.url = 'http://localhost:8084/registry-provisioning/newUid/save' // Use local mock server
        grailsApplication.config.rest.provisionUid.url = 'http://localhost:8084/registry-provisioning/provision/save' // Use local mock server
        SOR sor
        if (!(sor = SOR.findByName("HR"))) {
            sor = new SOR(name: 'HR').save(failOnError: true, flush: true)
        }
        if (!SORObject.findBySorAndSorPrimaryKey(sor, "HR0001")) {
            new SORObject(sor: sor, sorPrimaryKey: 'HR0001', queryTime: new Date(), jsonVersion: 1, objJson: "{}").save(failOnError: true, flush: true)
        }
        if (!Person.findByUid("002")) {
            new Person(uid: '002', dateOfBirth: Date.parse('yyy-MM-dd', '1999-09-09')).save(failOnError: true, flush: true)
        }
        if (!Person.findByUid("003")) {
            new Person(uid: '003', dateOfBirth: Date.parse('yyy-MM-dd', '1999-09-09')).save(failOnError: true, flush: true)
        }
    }

    static ActiveMQMapMessage createJmsMessage(Map data) {
        ActiveMQMapMessage jmsMsg = new ActiveMQMapMessage()
        data.each { key, value ->
            jmsMsg.setString(key, value)
        }
        return jmsMsg
    }

    @Rollback
    @Unroll
    def 'when entering the system with a SORObject that does not match an existing person, expect to see the new created UID on the provisioning queue: #description'() {
        given:
        def data = [systemOfRecord: "HR", sorPrimaryKey: "HR0001", givenName: 'FirstName', surName: 'LastName', dateOfBirth: '1988-01-01']
        def sorObject = SORObject.getBySorAndObjectKey(data.systemOfRecord, data.sorPrimaryKey)
        matchEngine.registerPost('/ucb-match/v1/person', statusCode: HttpStatus.NOT_FOUND.value())
        uidService.registerPost("/registry-provisioning/newUid/save?sorObjectId=${sorObject.id}&synchronousDownstream=true", statusCode: HttpStatus.OK.value(), json: [uid: '001', sorObjectId: '2', provisioningSuccessful: true])
        when:
        Map result = newSORConsumerService.onMessage(createJmsMessage(data))
        then:
        result.matchType == "noMatch"
        result.uid == "001"
        matchEngine.verify()
        uidService.verify()
        where:
        description                          | synchronousDownstream
        "provision downstream synchronously" | true
    }


    @Rollback
    def 'when entering the system with a SORObject that does match an single existing person, expect to see that persons UID on the provisioning queue'() {
        given:
        def person = Person.get('002')
        matchEngine.registerPost('/ucb-match/v1/person', statusCode: HttpStatus.OK.value(), json: [matchingRecord: [referenceId: '002', ruleNames: ["Canonical #1"]]])
        uidService.registerPost("/registry-provisioning/provision/save?uid=${person.uid}&synchronousDownstream=true", statusCode: HttpStatus.OK.value(), json: [uid: '001', sorObjectId: '2', provisioningSuccessful: true])
        def data = [systemOfRecord: "HR", sorPrimaryKey: "HR0001", givenName: 'FirstName', surName: 'LastName', dateOfBirth: '1988-01-01']
        when:
        Map result = newSORConsumerService.onMessage(createJmsMessage(data))
        then:
        result.matchType == "exactMatch"
        result.uid == "002"
        matchEngine.verify()
        uidService.verify()
    }

    @Rollback
    def 'when entering the system with a SORObject that matches multiple existing persons, do not expect to see a response on the queue but instead expect to find two rows in the PartialMatch table'() {
        given:
        matchEngine.registerPost('/ucb-match/v1/person', statusCode: HttpStatus.MULTIPLE_CHOICES.value(), json: [partialMatchingRecords: [[referenceId: '002', ruleNames: ["Potential #1", "Potential #2"]], [referenceId: '003', ruleNames: ["Potential #2"]]]])
        def data = [systemOfRecord: "HR", sorPrimaryKey: "HR0001", givenName: 'FirstName', surName: 'LastName', dateOfBirth: '1988-01-01']
        when:
        newSORConsumerService.onMessage(createJmsMessage(data))
        def rows = PartialMatch.list()

        then: "Expect a timeout from the jms queue"
        matchEngine.verify()
        and:
        rows.size() == 2
        rows.collect { it.person.id }.sort() == ['002', '003']
    }

    def 'when entering the system with a SORObject that does match an single existing person, expect to see all PartialMatches for that SORObject to be deleted'() {
        given:
        PartialMatch.withNewTransaction {
            PartialMatch.list().each {
                it.delete(failOnError: true, flush: true)
            }
        }
        Person.withNewTransaction {
            def personPartialMatches = [new PersonPartialMatch(Person.get('002'), ['Potential #1']), new PersonPartialMatch(Person.get('003'), ['Potential #2'])]
            databaseService.storePartialMatch(SORObject.findBySorPrimaryKeyAndSor("HR0001", SOR.findByName("HR")), personPartialMatches)
        }
        SORObject.withNewTransaction {
            matchEngine.registerPost('/ucb-match/v1/person', statusCode: HttpStatus.OK.value(), json: [matchingRecord: [referenceId: '002']])
        }
        def data = [systemOfRecord: "HR", sorPrimaryKey: "HR0001", givenName: 'FirstName', surName: 'LastName', dateOfBirth: '1988-01-01']

        when:
        def pmSize = 0
        PartialMatch.withNewTransaction {
            pmSize = PartialMatch.list().size()
        }
        assert pmSize == 2
        PartialMatch.withNewTransaction {
            PartialMatch.list().each {
                it.save(flush: true, failOnError: true)
            }
        }
        Map result = newSORConsumerService.onMessage(createJmsMessage(data))
        def rows = null
        PartialMatch.withNewTransaction {
            rows = PartialMatch.list()
        }

        then:
        result.matchType == "exactMatch"
        result.uid == "002"
        matchEngine.verify()
        rows.size() == 0
    }
}
