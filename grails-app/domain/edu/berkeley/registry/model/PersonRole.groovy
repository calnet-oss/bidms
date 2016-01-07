package edu.berkeley.registry.model

import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode
import org.hibernate.FetchMode

@ConverterConfig(excludes = ["person"])
@LogicalEqualsAndHashCode(excludes = ["person"])
class PersonRole implements Comparable {
    Long id
    AssignableRole role
    boolean deleted

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['role']
    }

    static mapping = {
        table name: "PersonRole"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'PersonRole_seq'], sqlType: 'BIGINT'
        role column: 'roleId', sqlType: 'INTEGER', fetch: FetchMode.JOIN
        person column: PersonRole.getUidColumnName(), sqlType: 'VARCHAR(64)'
        deleted column: 'isDeleted', sqlType: 'BOOLEAN'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("PersonRole", "uid")
    }

    int compareTo(obj) {
        return logicalHashCode() <=> obj.logicalHashCode() ?: hashCode() <=> obj.hashCode()
    }
}
