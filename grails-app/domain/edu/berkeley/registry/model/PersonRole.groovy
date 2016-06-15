package edu.berkeley.registry.model

import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import org.hibernate.FetchMode

// roleCategory and roleAsgnUniquePerCat are part of AssignableRole and are
// used here in this class as a foreign key reference for indexing purposes
@ConverterConfig(excludes = ["person", "roleCategory", "roleAsgnUniquePerCat"])
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "person", "roleCategory", "roleAsgnUniquePerCat"])
class PersonRole implements Comparable {
    Long id
    AssignableRole role
    String roleValue
    boolean roleAsgnUniquePerCat

    /**
     * Note that roleCategory+roleAsgnUniquePerCat is a composite foreign
     * key reference to AssignableRoleCategory.  I don't know how to model
     * that composite foreign key reference in GORM, so we make do with a
     * single key foreign key reference for roleCategory.
     */
    static belongsTo = [person: Person, roleCategory: AssignableRoleCategory]

    static constraints = {
        person unique: ['role', 'roleValue']
        /**
         * There is also a unique constraint in the DB using a partial index
         * "ON PersonRole(uid, roleCategoryId) WHERE roleAsgnUniquePerCat =
         * true".  I don't know how to model partial indexes in GORM.
         */
        roleValue nullable: true, size: 1..255
    }

    static mapping = {
        table name: "PersonRole"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'PersonRole_seq'], sqlType: 'BIGINT'
        role column: 'roleId', sqlType: 'INTEGER', fetch: FetchMode.JOIN
        person column: PersonRole.getUidColumnName(), sqlType: 'VARCHAR(64)'
        roleValue column: 'roleValue', sqlType: 'VARCHAR(255)'
        roleCategory column: 'roleCategoryId', sqlType: 'INTEGER', fetch: FetchMode.JOIN
        roleAsgnUniquePerCat column: 'roleAsgnUniquePerCat', sqlType: 'BOOLEAN'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("PersonRole", "uid")
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}
