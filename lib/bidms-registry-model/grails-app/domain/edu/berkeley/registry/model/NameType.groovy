package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode
class NameType {

    Integer id
    String typeName
    SOR sor

    static constraints = {
        typeName unique: true
        sor nullable: true
    }

    static mapping = {
        table name: "NameType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        typeName column: 'typeName', sqlType: 'VARCHAR(64)'
        sor column: 'sorId'
    }
}
