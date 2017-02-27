package edu.berkeley.registry.model

import grails.test.hibernate.HibernateSpec

class PersonSpec extends HibernateSpec {
    private static final Map opts = [failOnError: true, flush: true]

    List<Class> getDomainClasses() { [Person] }

    /**
     * Since using 'uid' as the identifier property, this confirms Person
     * has a getId() and setId() method that retrieves and sets the uid
     * property.
     */
    void "save test"() {
        given:
        Person person = new Person(uid: "uid123")

        when:
        person.save(opts)

        then:
        person.id
    }
}
