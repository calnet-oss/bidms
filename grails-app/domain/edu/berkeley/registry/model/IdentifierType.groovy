package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode
class IdentifierType {

    Integer id
    String idName
    SOR sor

    static constraints = {
        idName unique: true
        sor nullable: true
    }

    static mapping = {
        table name: "IdentifierType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        idName column: 'idName', sqlType: 'VARCHAR(64)'
        sor column: 'sorId'
    }
}
