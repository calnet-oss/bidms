package edu.berkeley.registry.model

import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@ConverterConfig(excludes = ["person", "sorObject"])
@LogicalEqualsAndHashCode(excludes = ["person"])
class DateOfBirth implements Comparable {

    Long id
    SORObject sorObject
    String dateOfBirthMMDD
    Date dateOfBirth

    static belongsTo = [person: Person]

    static constraints = {
        person unique: 'sorObject'
        dateOfBirthMMDD nullable: true
        dateOfBirth nullable: true
    }

    static mapping = {
        table name: "DateOfBirth"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'DateOfBirth_seq'], sqlType: 'BIGINT'
        person column: DateOfBirth.getUidColumnName(), sqlType: 'VARCHAR(64)'
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        dateOfBirthMMDD column: 'dateOfBirthMMDD', sqlType: 'CHAR(4)'
        dateOfBirth column: 'dateOfBirth', sqlType: 'DATE'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("DateOfBirth", "uid")
    }

    int compareTo(obj) {
        return logicalHashCode() <=> obj.logicalHashCode() ?: hashCode() <=> obj.hashCode()
    }
}
