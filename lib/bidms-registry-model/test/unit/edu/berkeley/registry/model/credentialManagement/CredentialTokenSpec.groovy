package edu.berkeley.registry.model.credentialManagement

import edu.berkeley.registry.model.Identifier
import edu.berkeley.registry.model.IdentifierType
import edu.berkeley.registry.model.Person
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([CredentialToken, Person, IdentifierType, Identifier])
class CredentialTokenSpec extends Specification {
    def "test that object validates because beforeValidate assigns token"() {
        given:
        def identifierType = new IdentifierType(idName: 'sisStudentId').save(failOnError: true, flush: true)
        def person = new Person(uid: '123').save(failOnError: true, flush: true)
        def identifier = new Identifier(person: person, identifier: '567', identifierType: identifierType).save(validate: false, flush: true)

        when:
        def credentialToken = new CredentialToken(person: person, identifier: identifier, expiryDate: new Date() + 1)

        then:
        credentialToken.validate()
        credentialToken.token != null
    }
}
