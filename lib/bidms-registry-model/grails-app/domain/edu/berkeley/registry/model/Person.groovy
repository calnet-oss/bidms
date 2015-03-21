package edu.berkeley.registry.model

import java.sql.Timestamp

class Person {
    String uid
    String dateOfBirthMMDD
    Date dateOfBirth
    Timestamp timeCreated
    Timestamp timeUpdated

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
