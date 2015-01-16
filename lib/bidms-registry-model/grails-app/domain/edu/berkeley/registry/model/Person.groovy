package edu.berkeley.registry.model

class Person {
    String uid
    String firstName
    String lastName
    String dateOfBirth
    String socialSecurityNumber

    static constraints = {
    }

    static mapping = {
        id name: 'uid', generator: 'assigned'
    }
}
