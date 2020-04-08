package edu.berkeley.registry.model.credentialManagement

import edu.berkeley.registry.model.Identifier
import edu.berkeley.registry.model.IdentifierType
import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.SOR
import edu.berkeley.registry.model.SORObject
import grails.testing.gorm.DataTest
import spock.lang.Specification

class CredentialTokenSpec extends Specification implements DataTest {
    void setupSpec() {
        mockDomains CredentialToken, Person, IdentifierType, Identifier, SORObject
    }

    def "test that object validates because beforeValidate assigns token"() {
        given:
        def identifierType = new IdentifierType(idName: 'sisStudentId').save(failOnError: true, flush: true)
        def person = new Person(uid: '123').save(failOnError: true, flush: true)
        def sor = new SOR(sorName: "TEST_SOR").save(validate: false, flush: true)
        def sorObject = new SORObject(person: person, sor: sor, sorPrimaryKey: "test", queryTime: new Date(), objJson: "{}", jsonVersion: 1, hash: "{}".hashCode()).save(validate: false, flush: true)
        def identifier = new Identifier(person: person, sorObject: sorObject, identifier: '567', identifierType: identifierType).save(validate: false, flush: true)

        when:
        def credentialToken = new CredentialToken(person: person, identifier: identifier, expiryDate: new Date() + 1)

        then:
        credentialToken.validate()
        credentialToken.token != null
    }
}
