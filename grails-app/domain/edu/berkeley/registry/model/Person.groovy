package edu.berkeley.registry.model

class Person {

    // 'id' can be ignored and is not used, but seems to be necessary to
    // declare it here as a String in order to avoid a bug in Grails (last
    // tested with 2.4.4).  uid is instead used as the primary key.
    String id

    String uid
    String dateOfBirthMMDD
    Date dateOfBirth
    Date timeCreated
    Date timeUpdated

    static hasMany = [ names : PersonName ]

    static constraints = {
        dateOfBirthMMDD nullable: true
        dateOfBirth nullable: true
        timeCreated nullable: true // assigned automatically by db trigger
        timeUpdated nullable: true // assigned automatically by db trigger
    }

    static mapping = {
        version false
        id name: 'uid', column: 'uid', generator: 'assigned', sqlType: 'VARCHAR(64)'
        dateOfBirthMMDD column: 'dateOfBirthMMDD', sqlType: 'CHAR(4)'
        dateOfBirth column: 'dateOfBirth'
        timeCreated column: 'timeCreated', insertable: false, updateable: false
        timeUpdated column: 'timeUpdated', insertable: false, updateable: false
    }
}
