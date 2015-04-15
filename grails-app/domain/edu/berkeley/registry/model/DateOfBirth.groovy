package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode(excludes = ["person"])
class DateOfBirth {

    Long id
    String dateOfBirthMMDD
    Date dateOfBirth
    SOR sor

    static belongsTo = [person: Person]

    static constraints = {
        dateOfBirthMMDD nullable: true
        dateOfBirth nullable: true
    }

    static mapping = {
        table name: "DateOfBirth"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'DateOfBirth_seq'], sqlType: 'BIGINT'
        person column: 'uid', sqlType: 'VARCHAR(64)'
        sor column: 'sorId'
        dateOfBirthMMDD column: 'dateOfBirthMMDD', sqlType: 'CHAR(4)'
        dateOfBirth column: 'dateOfBirth', sqlType: 'DATE'
    }
}
