package edu.berkeley.registry.model

class PersonSorObjectsSyncKey {

    String id // uid
    String provisionedJsonHash
    Boolean forceProvision
    Date timeUpdated // normally updated by a trigger, but sometimes we want to force persistence by modifying this column

    static constraints = {
        provisionedJsonHash nullable: true
        timeUpdated nullable: true // is NOT NULL but will be filled in by the trigger if null
    }

    static mapping = {
        table name: "PersonSorObjectsSyncKey"
        version false
        id column: 'uid', generator: 'assigned', sqlType: 'VARCHAR(64)'
        provisionedJsonHash column: 'provisionedJsonHash', sqlType: 'TEXT'
        forceProvision column: 'forceProvision', sqlType: 'BOOLEAN'
        timeUpdated column: 'timeUpdated'
    }
}
