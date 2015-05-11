package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode
class TelephoneType {
    Integer id
    String shortDescription

    static constraints = {
        // 'unique' GRAILS BUG: UNCOMMENT WHEN FIXED: https://jira.grails.org/browse/GRAILS-11600
        //typeName unique: true
        shortDescription size: 1..64
    }

    static mapping = {
        table name: "TelephoneType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        shortDescription column: 'typeName', sqlType: 'VARCHAR(64)'
    }
}
