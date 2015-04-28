package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode(excludes = ["person"])
class Identifier {

    Long id
    IdentifierType identifierType
    SORObject sorObject
    String identifier

    static belongsTo = [person: Person]

    static constraints = {
        // 'unique' GRAILS BUG: UNCOMMENT WHEN FIXED: https://jira.grails.org/browse/GRAILS-11600
        //person unique: ['sorObject', 'identifierType']
    }

    static mapping = {
        table name: "Identifier"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'Identifier_seq'], sqlType: 'BIGINT'
        person column: 'uid', sqlType: 'VARCHAR(64)'
        identifierType column: 'identifierTypeId', sqlType: 'SMALLINT'
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        identifier column: 'identifier', sqlType: 'VARCHAR(64)'
    }
}
