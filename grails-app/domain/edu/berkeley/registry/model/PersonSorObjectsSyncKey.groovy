package edu.berkeley.registry.model

class PersonSorObjectsSyncKey {

    String id // uid
    String provisionedJsonHash
    Boolean forceProvision

    static constraints = {
        provisionedJsonHash nullable: true
    }

    static mapping = {
        table name: "PersonSorObjectsSyncKey"
        version false
        id column: 'uid', generator: 'assigned', sqlType: 'VARCHAR(64)'
        provisionedJsonHash column: 'provisionedJsonHash', sqlType: 'TEXT'
        forceProvision column: 'forceProvision', sqlType: 'BOOLEAN'
    }
}
