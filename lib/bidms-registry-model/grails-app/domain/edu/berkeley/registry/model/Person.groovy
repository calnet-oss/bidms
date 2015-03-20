package edu.berkeley.registry.model

class Person {
    String uid
    String dateOfBirthMMDD
    Date dateOfBirth

    static constraints = {
        uid unique: true
        dateOfBirthMMDD nullable: true
        dateOfBirth nullable: true
    }

    static mapping = {
        id name: 'uid', column: 'uid', generator: 'assigned'
        version false
        dateOfBirthMMDD column: 'dateOfBirthMMDD'
        dateOfBirth column: 'dateOfBirth'
    }
}
