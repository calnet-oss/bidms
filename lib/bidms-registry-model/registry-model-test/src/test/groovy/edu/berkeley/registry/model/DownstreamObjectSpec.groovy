package edu.berkeley.registry.model

import edu.berkeley.registry.model.types.DownstreamObjectOwnershipLevelEnum
import edu.berkeley.registry.model.types.DownstreamSystemEnum
import grails.testing.gorm.DataTest
import groovy.json.JsonBuilder

class DownstreamObjectSpec extends AbstractDomainObjectSpec implements DataTest {
    DownstreamSystem testDownstreamSystem
    Person testPerson

    void setupSpec() {
        mockDomains DownstreamObject, DownstreamSystem, Person
    }

    def setup() {
        testDownstreamSystem = new DownstreamSystem(name: DownstreamSystemEnum.LDAP.name())
        testDownstreamSystem.save(flush: true, failOnError: true)

        testPerson = new Person(uid: "person1")
        testPerson.save(flush: true, failOnError: true)
    }

    Class<?> getDomainClass() { return DownstreamObject }

    void "confirm DownstreamObject using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm DownstreamObject LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person", "json"])
    }

    void "confirm DownstreamObject logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["systemPrimaryKey", "objJson", "hash", "ownershipLevel", "globUniqId", "forceProvision", "system"])
    }

    def "test that a DownstreamObject can be found when exists"() {
        given:
        assert DownstreamSystem.get(testDownstreamSystem.id) != null
        def obj = new DownstreamObject(
                person: Person.get("person1"),
                system: DownstreamSystem.findById(testDownstreamSystem.id),
                systemPrimaryKey: '123',
                objJson: '{}',
                ownershipLevel: DownstreamObjectOwnershipLevelEnum.OWNED.value
        ).save(flush: true, failOnError: true)

        expect:
        obj.id

        and:
        DownstreamObject.findBySystemAndSystemPrimaryKey(DownstreamSystem.findByName(DownstreamSystemEnum.LDAP.name()), '123')
    }

    def "test that finding a DownstreamObject with unknown system or key returns null"() {
        given:
        def obj = new DownstreamObject(
                person: Person.get("person1"),
                system: DownstreamSystem.findByName(DownstreamSystemEnum.LDAP.name()),
                systemPrimaryKey: '123',
                objJson: '{}',
                ownershipLevel: DownstreamObjectOwnershipLevelEnum.OWNED.value
        ).save(flush: true, failOnError: true)

        expect:
        obj.id

        and:
        !DownstreamObject.findBySystemAndSystemPrimaryKey(null, '123')
    }

    def "test parsing json"() {
        given:
        def json = new JsonBuilder([name: 'archer', middleName: null]).toString()
        def obj = new DownstreamObject(
                person: Person.get("person1"),
                system: DownstreamSystem.findByName(DownstreamSystemEnum.LDAP.name()),
                systemPrimaryKey: '123',
                objJson: json,
                ownershipLevel: DownstreamObjectOwnershipLevelEnum.OWNED.value
        ).save(flush: true, failOnError: true)

        expect:
        obj.json.name == 'archer'
        obj.json.containsKey("middleName")
    }

    void "confirm has hash code change callback"() {
        expect:
        testHasHashCodeChangeCallback()
    }
}