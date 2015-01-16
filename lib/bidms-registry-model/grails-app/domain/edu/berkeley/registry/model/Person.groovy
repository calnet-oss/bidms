package edu.berkeley.registry.model

class Person {
    String uid
    String firstName
    String lastName
    String dateOfBirth
    String socialSecurityNumber

    static constraints = {
        uid unique: true
    }

    static mapping = {
        id name: 'uid', column: 'UID', generator: 'assigned'
        version false
        firstName column: 'firstName'
        lastName column: 'lastName'
        dateOfBirth column: 'dateOfBirth'
        socialSecurityNumber column: 'socialSecurityNumber'
    }
}
