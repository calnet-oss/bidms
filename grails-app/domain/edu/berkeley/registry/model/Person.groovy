package edu.berkeley.registry.model

class Person {

    String id // uid
    Date timeCreated
    Date timeUpdated

    static hasMany = [
            addresses   : Address,
            names       : PersonName,
            datesOfBirth: DateOfBirth,
            identifiers : Identifier,
            emails      : Email,
            telephones  : Telephone
    ]

    static constraints = {
        timeCreated nullable: true // assigned automatically by db trigger
        timeUpdated nullable: true // assigned automatically by db trigger
    }


    static mapping = {
        table name: "Person"
        version false
        id name: 'uid', column: 'uid', generator: 'assigned', sqlType: 'VARCHAR(64)'
        timeCreated column: 'timeCreated', insertable: false, updateable: false
        timeUpdated column: 'timeUpdated', insertable: false, updateable: false
        names cascade: "all-delete-orphan", batchSize: 25
        telephones cascade: "all-delete-orphan", batchSize: 25
        addresses cascade: "all-delete-orphan", batchSize: 25
        emails cascade: "all-delete-orphan", batchSize: 25
        datesOfBirth cascade: "all-delete-orphan", batchSize: 25
        identifiers cascade: "all-delete-orphan", batchSize: 25

    }

    static transients = ['uid']

    public String getUid() { return id }

    public void setUid(String uid) { this.id = uid }
}
