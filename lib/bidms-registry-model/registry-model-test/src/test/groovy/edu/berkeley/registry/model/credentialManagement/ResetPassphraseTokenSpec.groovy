package edu.berkeley.registry.model.credentialManagement

import edu.berkeley.registry.model.Person
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([ChangeEmailToken, Person, ResetPassphraseToken])
class ResetPassphraseTokenSpec extends Specification {
    def "test that object validates because beforeValidate assigns token"() {
        given:
        def person = new Person(uid: '123').save(failOnError: true, flush: true)

        when:
        def resetPassphraseToken= new ResetPassphraseToken(person: person, expiryDate: new Date() + 1)

        then:
        resetPassphraseToken.validate()
        resetPassphraseToken.token != null
    }
}
