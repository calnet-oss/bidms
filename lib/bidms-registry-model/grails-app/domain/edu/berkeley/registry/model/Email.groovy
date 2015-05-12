package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode(excludes = ["person"])
class Email {
    Long id
    EmailType emailType
    SORObject sorObject
    String emailAddress

    static belongsTo = [person: Person]

    static constraints = {
        // 'unique' GRAILS BUG: UNCOMMENT WHEN FIXED: https://jira.grails.org/browse/GRAILS-11600
        //person unique: ['sorObject', 'addressType']
        emailAddress email: true, size: 1..255

    }

    static mapping = {
        table name: "Email"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'Email_seq'], sqlType: 'BIGINT'
        emailType column: 'emailTypeId', sqlType: 'SMALLINT'
        person column: 'uid', sqlType: 'VARCHAR(64)'
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        emailAddress column: 'emailAddress', sqlType: 'VARCHAR(255)'
    }
}
