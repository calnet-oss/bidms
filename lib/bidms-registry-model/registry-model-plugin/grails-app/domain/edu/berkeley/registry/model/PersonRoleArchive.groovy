package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import org.hibernate.FetchMode

// roleCategory and roleAsgnUniquePerCat are part of AssignableRole and are
// used here in this class as a foreign key reference for indexing purposes
@ConverterConfig(excludes = ["person", "roleCategory", "roleAsgnUniquePerCat", "endOfRoleGraceTimeUseOverrideIfLater"])
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version", "person", "roleCategory", "originalPersonRoleId", "roleAsgnUniquePerCat", "timeCreated", "timeUpdated", "endOfRoleGraceTimeUseOverrideIfLater"])
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
        // startOfRoleGraceTime must be set
        // set to current time if the start-of-grace is unknown
        startOfRoleGraceTime validator: { startOfRoleGraceTimeVal, obj, errors ->
            if (!startOfRoleGraceTimeVal) {
                errors.rejectValue("startOfRoleGraceTime", "startOfRoleGraceTime can't be null")
            }
            // startOfRoleGraceTime can't be in the future
            if (startOfRoleGraceTimeVal > new Date() + 1) {
                errors.rejectValue("startOfRoleGraceTime", "startOfRoleGraceTime can't be set to a future date")
            }
            // startOfRoleGraceTime can't be later than endOfRoleGraceTimeUseOverrideIfLater if it's not null
            if (obj.endOfRoleGraceTimeUseOverrideIfLater && startOfRoleGraceTimeVal > obj.endOfRoleGraceTimeUseOverrideIfLater) {
                errors.rejectValue("startOfRoleGraceTime", "startOfRoleGraceTime can't be set to a value later than endOfRoleGraceTimeUseOverrideIfLater")
            }
        }
        endOfRoleGraceTime nullable: true, validator: { endOfRoleGraceTimeVal, obj, errors ->
            // if not null, endOfRoleGraceTime can't be earlier than startOfRoleGraceTime
            if (endOfRoleGraceTimeVal && endOfRoleGraceTimeVal < obj.startOfRoleGraceTime) {
                errors.rejectValue("endOfRoleGraceTime", "endOfRoleGraceTime can't be set to a value earlier than startOfRoleGraceTime")
            }
        }
        endOfRoleGraceTimeOverride nullable: true, validator: { endOfRoleGraceTimeOverrideVal, obj, errors ->
            // if not null, endOfRoleGraceTimeOverride can't be earlier than startOfRoleGraceTime
            if (endOfRoleGraceTimeOverrideVal && endOfRoleGraceTimeOverrideVal < obj.startOfRoleGraceTime) {
                errors.rejectValue("endOfRoleGraceTimeOverride", "endOfRoleGraceTimeOverride can't be set to a value earlier than startOfRoleGraceTime")
            }
        }
        roleInGrace validator: { roleInGraceVal, obj, errors ->
            // one and only one of roleInGrace or rolePostGrace must be true
            if (roleInGraceVal == obj.rolePostGrace) {
                errors.rejectValue("roleInGrace", "roleInGrace and rolePostGrace can't both be $roleInGraceVal: one and only one must be true")
            }
            // roleInGrace can't be true if the endOfRoleGraceTimeUseOverrideIfLater, if it's not null, is in the past
            if (roleInGraceVal && obj.endOfRoleGraceTimeUseOverrideIfLater && obj.endOfRoleGraceTimeUseOverrideIfLater < new Date() - 1) {
                errors.rejectValue("roleInGrace", "roleInGrace can't be true if endOfRoleGraceTimeUseOverrideIfLater is in the past, indicating post-grace")
            }
        }
        rolePostGrace validator: { rolePostGraceVal, obj, errors ->
            // one and only one of roleInGrace or rolePostGrace must be true
            if (rolePostGraceVal == obj.roleInGrace) {
                errors.rejectValue("rolePostGrace", "roleInGrace and rolePostGrace can't both be $rolePostGraceVal: one and only one must be true")
            }
            // rolePostGrace can't be true if endOfRoleGraceTimeUseOverrideIfLater, if it's not null, is in the future
            if (rolePostGraceVal && obj.endOfRoleGraceTimeUseOverrideIfLater && obj.endOfRoleGraceTimeUseOverrideIfLater > new Date() + 1) {
                errors.rejectValue("rolePostGrace", "rolePostGrace can't be true if endOfRoleGraceTimeUseOverrideIfLater is in the future, indicating in-grace")
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
