package edu.berkeley.registry.model

class Person {
    String uid
    String givenName
    String surName
    String dateOfBirth
    String socialSecurityNumber

    static constraints = {
        uid unique: true
        givenName nullable: true
        surName nullable: true
        dateOfBirth nullable: true
        socialSecurityNumber nullable: true
    }

    static mapping = {
        id name: 'uid', column: 'UID', generator: 'assigned'
        version false
        givenName column: 'givenName'
        surName column: 'surName'
        dateOfBirth column: 'dateOfBirth'
        socialSecurityNumber column: 'socialSecurityNumber'
    }
}
