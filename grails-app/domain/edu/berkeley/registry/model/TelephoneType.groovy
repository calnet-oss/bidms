package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode
class TelephoneType {
    Integer id
    String telephoneTypeName

    static constraints = {
        telephoneTypeName unique: true, size: 1..64
    }

    static mapping = {
        table name: "TelephoneType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        telephoneTypeName column: 'telephoneTypeName', sqlType: 'VARCHAR(64)'
    }
}
