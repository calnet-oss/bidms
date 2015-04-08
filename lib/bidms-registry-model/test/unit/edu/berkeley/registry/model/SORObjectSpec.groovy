package edu.berkeley.registry.model

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(SORObject)
@Mock([Person, PartialMatch, SORObject, PersonName, NameType, SOR])
class SORObjectSpec extends Specification {
    SOR testSOR

    def setup() {
        testSOR = new SOR(name: "SIS")
        testSOR.save(flush: true, failOnError: true)
    }

    def "test that a SORObject can be found when exists"() {
        given:
        assert SOR.get(testSOR.id) != null
        def obj = new SORObject(sor: SOR.findById(testSOR.id), sorPrimaryKey: '123').save(flush: true, validate: false, failOnError: true)

        expect:
        obj.id

        println SORObject.list()*.dump()

        and:
        SORObject.getBySorAndObjectKey('SIS', '123')
    }

    def "test that finding a SORObject with unknown sor or key returns null"() {
        given:
        def obj = new SORObject(sor: SOR.findById(1), sorPrimaryKey: '123').save(flush: true, validate: false)

        expect:
        obj.id

        and:
        !SORObject.getBySorAndObjectKey('HR','123')

        and:
        !SORObject.getBySorAndObjectKey('SIS','321')
    }

    @Unroll("should return json for object with person #hasPerson and object json #hasJson")
    void "should return map for object with person #hasPerson and object json #hasJson"() {
        given: "a person with a name"
            def nameType = new NameType(typeName: 'hrmsName', sor: testSOR).save(failOnError: true)
            def person = new Person(uid: '1000', dateOfBirthMMDD:'0590',dateOfBirth: Date.parse('yyyy-MM-dd', '1990-05-28')).save(failOnError: true)
            def personName = new PersonName(nameType: nameType, fullName: "Sterling M Archer", person: person).save(failOnError: true)
            person.addToNames(personName).save()
        and: "some raw json"
            def json = """
                {"cn": ["Archer, Sterling ", "Archer, Sterling M", "archer, sterling"], "dn": "uid=201070000,ou=people",
                "sn": ["Archer"], "uid": ["201070000"], "givenName": ["Sterling", "Archer M"], "displayName": "Sterling M Archer",
                "definitiveUid": "201070000", "employeeNumber": "012707709", "berkeleyEduAffID": ["5304188"],
                "berkeleyEduLastName": "Archer", "berkeleyEduFirstName": "Sterling"}
                """
        and: "a sor object"
            SORObject sorObject = new SORObject(sor:testSOR, objJson: "{}", sorPrimaryKey: "201070000", queryTime: new Date(), jsonVersion: 1).save(failOnError: true)
            if(hasPerson) sorObject.person = person
            if(hasJson) sorObject.objJson = json

        when:
            def results = sorObject.json
        then:
            results.id == sorObject.id
            results.sorPrimaryKey == sorObject.sorPrimaryKey
            results.queryTime == sorObject.queryTime?.format("yyyy-MM-dd'T'HH:mm:ssZ")
            results.jsonVersion == sorObject.jsonVersion
            if(hasJson) {
                assert results.objJson.displayName == JSON.parse(sorObject.objJson).displayName
                assert results.objJson.employeeNumber == JSON.parse(sorObject.objJson).employeeNumber
            } else {
                assert results.objJson == [:]
            }
            results.sorName == sorObject.sor.name
            if(hasPerson) {
                assert results.person.uid == person.uid
                assert results.person.names == person.names.collect{it.fullName}
            } else {
                assert results.person == null
            }

        where:
            hasPerson | hasJson
            false     | true
            false     | false
            true      | true
            true      | false
    }

}
