package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.DomainUtil

@LogicalEqualsAndHashCode(excludes = ["person"])
class Identifier {

    Long id
    IdentifierType identifierType
    SORObject sorObject
    String identifier

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['sorObject', 'identifierType']
    }

    static mapping = {
        table name: "Identifier"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'Identifier_seq'], sqlType: 'BIGINT'
        person column: Identifier.getUidColumnName(), sqlType: 'VARCHAR(64)'
        identifierType column: 'identifierTypeId', sqlType: 'SMALLINT'
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        identifier column: 'identifier', sqlType: 'VARCHAR(64)'
    }
    
    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("Identifier", "uid")
    }
}
