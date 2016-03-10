package edu.berkeley.registry.model.credentialManagement

import edu.berkeley.registry.model.Person
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([ChangeEmailToken, Person])
class ChangeEmailTokenSpec extends Specification {
    def "test that object validates because beforeValidate assigns token"() {
        given:
        def person = new Person(uid: '123').save(failOnError: true, flush: true)

        when:
        def changeEmailToken = new ChangeEmailToken(person: person, emailAddress: "valid@email.com", expiryDate: new Date() + 1)

        then:
        changeEmailToken.validate()
        changeEmailToken.token != null
    }
}
