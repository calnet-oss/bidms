package edu.berkeley.registry.model.credentialManagement

import edu.berkeley.registry.model.Person
import grails.testing.gorm.DataTest
import spock.lang.Specification

class ResetPassphraseTokenSpec extends Specification implements DataTest {
    void setupSpec() {
        mockDomains ChangeEmailToken, Person, ResetPassphraseToken
    }

    def "test that object validates because beforeValidate assigns token"() {
        given:
        def person = new Person(uid: '123').save(failOnError: true, flush: true)

        when:
        def resetPassphraseToken = new ResetPassphraseToken(person: person, expiryDate: new Date() + 1)

        then:
        resetPassphraseToken.validate()
        resetPassphraseToken.token != null
    }
}
