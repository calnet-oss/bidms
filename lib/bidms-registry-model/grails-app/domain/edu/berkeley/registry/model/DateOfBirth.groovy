package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode(excludes = ["person"])
class DateOfBirth {

    Long id
    SORObject sorObject
    String dateOfBirthMMDD
    Date dateOfBirth

    static belongsTo = [person: Person]

    static constraints = {
        // 'unique' GRAILS BUG: UNCOMMENT WHEN FIXED: https://jira.grails.org/browse/GRAILS-11600
        //person unique: 'sorObject'
        dateOfBirthMMDD nullable: true
        dateOfBirth nullable: true
    }

    static mapping = {
        table name: "DateOfBirth"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'DateOfBirth_seq'], sqlType: 'BIGINT'
        person column: 'uid', sqlType: 'VARCHAR(64)'
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        dateOfBirthMMDD column: 'dateOfBirthMMDD', sqlType: 'CHAR(4)'
        dateOfBirth column: 'dateOfBirth', sqlType: 'DATE'
    }
}
