package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode
class IdentifierType {

    Integer id
    String idName

    static constraints = {
        idName unique: true
    }

    static mapping = {
        table name: "IdentifierType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        idName column: 'idName', sqlType: 'VARCHAR(64)'
    }
}
