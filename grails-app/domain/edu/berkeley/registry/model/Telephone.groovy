package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode(excludes = ["person"])
class Telephone {
    Long id
    TelephoneType telephoneType
    SORObject sorObject
    String phoneNumber
    String extension

    static belongsTo = [person: Person]

    static constraints = {
        // 'unique' GRAILS BUG: UNCOMMENT WHEN FIXED: https://jira.grails.org/browse/GRAILS-11600
        //person unique: ['sorObject', 'addressType']
        extension nullable: true, size: 1..16
        phoneNumber size: 1..64

    }

    static mapping = {
        table name: "Telephone"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'Telephone_seq'], sqlType: 'BIGINT'
        telephoneType column: 'telephoneTypeId', sqlType: 'SMALLINT'
        person column: 'uid', sqlType: 'VARCHAR(64)'
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        phoneNumber column: 'phoneNumber', sqlType: 'VARCHAR(64)'
        extension column: 'extension', sqlType: 'VARCHAR(16)'
    }
}
