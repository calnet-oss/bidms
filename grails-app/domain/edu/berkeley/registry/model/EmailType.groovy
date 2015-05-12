package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode
class EmailType {
    Integer id
    String emailTypeName

    static constraints = {
        emailTypeName unique: true, size: 1..64
    }

    static mapping = {
        table name: "EmailType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        emailTypeName column: 'emailTypeName', sqlType: 'VARCHAR(64)'
    }
}
