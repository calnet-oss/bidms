package edu.berkeley.match

import edu.berkeley.registry.model.PartialMatch
import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.SOR
import edu.berkeley.registry.model.SORObject
import grails.test.spock.IntegrationSpec
import groovy.util.logging.Log4j
import org.apache.activemq.command.ActiveMQMapMessage
import org.codehaus.groovy.grails.orm.hibernate.cfg.DefaultGrailsDomainConfiguration
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import org.hibernate.tool.hbm2ddl.SchemaExport
import org.springframework.http.HttpStatus
import spock.lang.Shared

@Log4j
class NewSorConsumerServiceIntegrationSpec extends IntegrationSpec {
    @Shared
    private static Configuration _configuration

    @Shared
    def grailsApplication

    static transactional = false

    def newSORConsumerService

    @Shared
    VertxServer matchEngine = new VertxServer(host: 'localhost', port: 8082)
    @Shared
    VertxServer uidService = new VertxServer(host: 'localhost', port: 8084)

    def setupSpec() {
        if (!_configuration) {
            // 1-time creation of the configuration
            Properties properties = new Properties()
            properties.setProperty 'hibernate.connection.driver_class', grailsApplication.config.dataSource.driverClassName
            properties.setProperty 'hibernate.connection.username', grailsApplication.config.dataSource.username
            properties.setProperty 'hibernate.connection.password', grailsApplication.config.dataSource.password
            properties.setProperty 'hibernate.connection.url', grailsApplication.config.dataSource.url
            properties.setProperty 'hibernate.dialect', grailsApplication.config.dataSource.dialect

            _configuration = new DefaultGrailsDomainConfiguration(grailsApplication: grailsApplication, properties: properties)
        }

    }

    def cleanup() {
        //After spec nuke and pave the test db
        new SchemaExport(_configuration).create(false, true)

        //Clear the sessions
        SessionFactory sf = grailsApplication.getMainContext().getBean('sessionFactory')
        sf.getCurrentSession().clear()

        matchEngine.reset()
        uidService.reset()
    }

    def setup() {
        def sor = new SOR(name:'HR').save(failOnError: true, flush: true)
        new SORObject(sor: sor, sorPrimaryKey: 'HR0001', queryTime: new Date(), jsonVersion: 1, objJson: "{}").save(failOnError: true, flush: true)
        new Person(uid:'002',dateOfBirth: Date.parse('yyy-MM-dd' , '1999-09-09')).save(failOnError: true, flush: true)
        new Person(uid:'003',dateOfBirth: Date.parse('yyy-MM-dd' , '1999-09-09')).save(failOnError: true, flush: true)
    }

    static ActiveMQMapMessage createJmsMessage(Map data) {
        ActiveMQMapMessage jmsMsg = new ActiveMQMapMessage()
        data.each {key, value ->
            jmsMsg.setString(key, value)
        }
        return jmsMsg
    }

    def 'when entering the system with a SORObject that does not match an existing person, expect to see the new created UID on the provisioning queue'() {
        given:
            def data = [systemOfRecord: "HR", sorPrimaryKey: "HR0001", givenName: 'FirstName', surName: 'LastName', dateOfBirth: '1988-01-01']
            def sorObject = SORObject.getBySorAndObjectKey(data.systemOfRecord, data.sorPrimaryKey)
            matchEngine.registerPost('/ucb-match/v1/person', statusCode: HttpStatus.NOT_FOUND.value())
            uidService.registerPost("/registry-provisioning/newUid/save?sorObjectId=${sorObject.id}", statusCode: HttpStatus.OK.value(), json:[uid: '001', sorObjectId: '2', provisioningSuccessful: true])
        when:
            newSORConsumerService.onMessage(createJmsMessage(data))
        then:
            matchEngine.verify()
            uidService.verify()
    }
    def 'when entering the system with a SORObject that does match an single existing person, expect to see that persons UID on the provisioning queue'() {
        given:
            def person = Person.get('002')
            matchEngine.registerPost('/ucb-match/v1/person', statusCode: HttpStatus.OK.value(), json:[matchingRecord: [referenceId: '002']] )
            uidService.registerPost("/registry-provisioning/provision/save?uid=${person.uid}", statusCode: HttpStatus.OK.value(), json:[uid: '001', sorObjectId: '2', provisioningSuccessful: true])
            def data = [systemOfRecord: "HR", sorPrimaryKey: "HR0001", givenName: 'FirstName', surName: 'LastName', dateOfBirth: '1988-01-01']
        when:
            newSORConsumerService.onMessage(createJmsMessage(data))
        then:
            matchEngine.verify()
            uidService.verify()
    }

    def 'when entering the system with a SORObject that matches multiple existing persons, do not expect to see a response on the queue but instead expect to find two rows in the PartialMatch table'() {
        given:
            matchEngine.registerPost('/ucb-match/v1/person', statusCode: HttpStatus.MULTIPLE_CHOICES.value(), json:[partialMatchingRecords: [[referenceId: '002'], [referenceId: '003']]] )
            def data = [systemOfRecord: "HR", sorPrimaryKey: "HR0001", givenName: 'FirstName', surName: 'LastName', dateOfBirth: '1988-01-01']
        when:
            newSORConsumerService.onMessage(createJmsMessage(data))
            def rows = PartialMatch.list()

        then: "Expect a timeout from the jms queue"
            matchEngine.verify()
        and:
            rows.size() == 2
            rows.collect{it.person.id}.sort() == ['002','003']
    }
}

