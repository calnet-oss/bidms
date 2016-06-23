package edu.berkeley.match

import edu.berkeley.registry.model.PartialMatch
import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.SOR
import edu.berkeley.registry.model.SORObject
import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(DatabaseService)
@Build([SOR, SORObject, Person, PartialMatch])
class DatabaseServiceSpec extends Specification {
    SORObject sorObject
    PartialMatch existingPartialMatch
    Person person1
    Person person2

    def setup() {
        createModel()
    }

    void "when assigning a uid to a SORObject it will be persisted"() {
        expect:
        sorObject.person == null

        when:
        service.assignUidToSOR(sorObject, person1)

        then:
        sorObject.person == person1
    }

    void "when storing partial match not previously exsting a new PartialMatch is created"() {
        given:
        def sorObject2 = SORObject.build(sor: SOR.build(name: 'HR'), sorPrimaryKey: 'HR123')
        def person3 = Person.build(uid: '3')

        expect:
        PartialMatch.countBySorObject(sorObject2) == 0

        when:
        service.storePartialMatch(sorObject2, [person2, person3])

        then:
        PartialMatch.countBySorObject(sorObject2) == 2
        PartialMatch.countBySorObjectAndPerson(sorObject2, person2) == 1
        PartialMatch.countBySorObjectAndPerson(sorObject2, person3) == 1
    }

    void "when storing partial match on existing PartialMatch, where there was only one match the correct update takes place"() {
        expect:
        PartialMatch.countBySorObject(sorObject) == 1

        when:
        service.storePartialMatch(sorObject, [person1, person2])

        then:
        PartialMatch.countBySorObject(sorObject) == 2
        PartialMatch.countBySorObjectAndPerson(sorObject, person1) == 1
        PartialMatch.countBySorObjectAndPerson(sorObject, person2) == 1
    }

    void "when assigning a new uid to a SORObject in the PartialMatch table, confirm the PartialMatch is removed"() {
        expect:
        PartialMatch.countBySorObject(sorObject) == 1

        when:
        service.storePartialMatch(sorObject, [person1, person2])
        assert PartialMatch.countBySorObject(sorObject) == 2
        service.assignUidToSOR(sorObject, person1)

        then:
        PartialMatch.countBySorObject(sorObject) == 0
    }

    private createModel() {
        def sor = SOR.build(name: 'SIS')
        sorObject = SORObject.build(sor: sor, sorPrimaryKey: 'SIS123')
        person1 = Person.build(uid: 1)
        person2 = Person.build(uid: 2)
        existingPartialMatch = PartialMatch.build(sorObject: sorObject, person: person1)
    }
}
