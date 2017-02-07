package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import org.hibernate.FetchMode

// roleCategory and roleAsgnUniquePerCat are part of AssignableRole and are
// used here in this class as a foreign key reference for indexing purposes
@ConverterConfig(excludes = ["person", "roleCategory", "roleAsgnUniquePerCat", "endOfRoleGraceTimeUseOverrideIfLater"])
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "person", "roleCategory", "originalPersonRoleId", "roleAsgnUniquePerCat", "timeCreated", "timeUpdated", "endOfRoleGraceTimeUseOverrideIfLater"])
class PersonRoleArchive implements Comparable {
    Long id
    AssignableRole role
    Long originalPersonRoleId
    boolean roleAsgnUniquePerCat
    Date startOfRoleGraceTime
    Date endOfRoleGraceTime
    // endOfRoleGraceTimeOverride is manually set and overrides the
    // automatically calculated endOfRoleGraceTime
    Date endOfRoleGraceTimeOverride
    Date originalTimeCreated
    Date originalTimeUpdated
    boolean roleInGrace
    boolean rolePostGrace
    Date timeCreated
    Date timeUpdated

    static belongsTo = [person: Person, roleCategory: AssignableRoleCategory]

    static constraints = {
        person unique: ['role']
        /**
         * There is also a unique constraint in the DB using a partial index
         * "ON PersonRoleArchive(uid, roleCategoryId) WHERE roleAsgnUniquePerCat =
         * true".  I don't know how to model partial indexes in GORM.
         */
        originalPersonRoleId nullable: true
        // startOfRoleGraceTime must be set and can't be in the future
        // set to current time if the start-of-grace is unknown
        startOfRoleGraceTime max: new Date() + 1
        endOfRoleGraceTime nullable: true
        endOfRoleGraceTimeOverride nullable: true
        roleInGrace validator: { roleInGraceVal, obj, errors ->
            // one and only one of roleInGrace or rolePostGrace must be true
            if (!roleInGraceVal && !obj.rolePostGrace) {
                errors.rejectValue("roleInGrace", "roleInGrace and rolePostGrace can't both be false: only one must be true")
            }
            else if (roleInGraceVal && obj.rolePostGrace) {
                errors.rejectValue("roleInGrace", "roleInGrace and rolePostGrace can't both be true: only one must be true")
            }
        }
        rolePostGrace validator: { rolePostGraceVal, obj, errors ->
            // one and only one of roleInGrace or rolePostGrace must be true
            if (!rolePostGraceVal && !obj.roleInGrace) {
                errors.rejectValue("rolePostGrace", "roleInGrace and rolePostGrace can't both be false: only one must be true")
            }
            else if (rolePostGraceVal && obj.roleInGrace) {
                errors.rejectValue("rolePostGrace", "roleInGrace and rolePostGrace can't both be true: only one must be true")
            }
        }
        timeCreated nullable: true // assigned automatically by db trigger
        timeUpdated nullable: true // assigned automatically by db trigger
    }

    static mapping = {
        table name: "PersonRoleArchive"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'PersonRoleArchive_seq'], sqlType: 'BIGINT'
        role column: 'roleId', sqlType: 'INTEGER', fetch: FetchMode.JOIN
        person column: PersonRoleArchive.getUidColumnName(), sqlType: 'VARCHAR(64)'
        roleCategory column: 'roleCategoryId', sqlType: 'INTEGER', fetch: FetchMode.JOIN
        originalPersonRoleId column: 'originalPersonRoleId', sqlType: 'BIGINT'
        roleAsgnUniquePerCat column: 'roleAsgnUniquePerCat', sqlType: 'BOOLEAN'
        startOfRoleGraceTime column: 'startOfRoleGraceTime', sqlType: 'TIMESTAMP'
        endOfRoleGraceTime column: 'endOfRoleGraceTime', sqlType: 'TIMESTAMP'
        endOfRoleGraceTimeOverride column: 'endOfRoleGraceTimeOverride', sqlType: 'TIMESTAMP'
        originalTimeCreated column: 'originalTimeCreated', sqlType: 'TIMESTAMP'
        originalTimeUpdated column: 'originalTimeUpdated', sqlType: 'TIMESTAMP'
        roleInGrace column: 'roleInGrace', sqlType: 'BOOLEAN'
        rolePostGrace column: 'rolePostGrace', sqlType: 'BOOLEAN'
        timeCreated column: 'timeCreated', insertable: false, updateable: false
        timeUpdated column: 'timeUpdated', insertable: false, updateable: false
    }

    static transients = ['endOfRoleGraceTimeUseOverrideIfLater']

    /**
     * @return endOfRoleGraceTimeOverride if it has a value AND is later
     *         than endOfRoleGraceTime, otherwise will return
     *         endOfRoleGraceTime
     */
    Date getEndOfRoleGraceTimeUseOverrideIfLater() {
        // CNR-1215: Decided that override should only be used if it not
        // only exists, but is later than endOfRoleGraceTime.
        return (endOfRoleGraceTimeOverride > endOfRoleGraceTime ? endOfRoleGraceTimeOverride : endOfRoleGraceTime)
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
