package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode
class AddressType {
    Integer id
    String typeName

    static constraints = {
        // 'unique' GRAILS BUG: UNCOMMENT WHEN FIXED: https://jira.grails.org/browse/GRAILS-11600
        //typeName unique: true
        typeName size: 1..64
    }

    static mapping = {
        table name: "AddressType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        typeName column: 'typeName', sqlType: 'VARCHAR(64)'
    }
}
