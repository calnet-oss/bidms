package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import org.hibernate.FetchMode

// roleCategory and roleAsgnUniquePerCat are part of AssignableRole and are
// used here in this class as a foreign key reference for indexing purposes
@ConverterConfig(excludes = ["person", "roleCategory", "roleAsgnUniquePerCat"])
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "person", "roleCategory", "roleAsgnUniquePerCat"])
class PersonRoleArchive implements Comparable {
    Long id
    AssignableRole role
    boolean roleAsgnUniquePerCat
    Date startOfRoleGraceTime

    static belongsTo = [person: Person, roleCategory: AssignableRoleCategory]

    static constraints = {
        person unique: ['role']
        /**
         * There is also a unique constraint in the DB using a partial index
         * "ON PersonRoleArchive(uid, roleCategoryId) WHERE roleAsgnUniquePerCat =
         * true".  I don't know how to model partial indexes in GORM.
         */
        startOfRoleGraceTime nullable: true
    }

    static mapping = {
        table name: "PersonRoleArchive"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'PersonRoleArchive_seq'], sqlType: 'BIGINT'
        person column: PersonRoleArchive.getUidColumnName(), sqlType: 'VARCHAR(64)'
        roleCategory column: 'roleCategoryId', sqlType: 'INTEGER', fetch: FetchMode.JOIN
        roleAsgnUniquePerCat column: 'roleAsgnUniquePerCat', sqlType: 'BOOLEAN'
        startOfRoleGraceTime column: 'startOfRoleGraceTime', sqlType: 'TIMESTAMP'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("PersonRoleArchive", "uid")
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}
