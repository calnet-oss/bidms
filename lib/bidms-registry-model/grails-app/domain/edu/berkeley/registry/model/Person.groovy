package edu.berkeley.registry.model

import org.codehaus.groovy.grails.exceptions.GrailsRuntimeException

class Person {

    String id // uid
    Date timeCreated
    Date timeUpdated
    boolean isLocked

    // We use sorted sets so the sets are ordered the same way each time a
    // person is queried.  This is particularly relevant for JSON
    // generation.  We want the JSON output to look the same each time.
    SortedSet<Address> addresses
    SortedSet<PersonName> names
    SortedSet<DateOfBirth> datesOfBirth
    SortedSet<Identifier> identifiers
    SortedSet<Email> emails
    SortedSet<Telephone> telephones
    SortedSet<PersonRole> assignedRoles
    SortedSet<TrackStatus> trackStatuses
    SortedSet<DelegateProxy> delegations
    SortedSet<DownstreamObject> downstreamObjects
    SortedSet<JobAppointment> jobAppointments
    SortedSet<IdentifierArchive> archivedIdentifiers
    SortedSet<PersonRoleArchive> archivedRoles

    static hasMany = [
            addresses          : Address,
            names              : PersonName,
            datesOfBirth       : DateOfBirth,
            identifiers        : Identifier,
            emails             : Email,
            telephones         : Telephone,
            assignedRoles      : PersonRole,
            trackStatuses      : TrackStatus,
            delegations        : DelegateProxy,
            downstreamObjects  : DownstreamObject,
            jobAppointments    : JobAppointment,
            archivedIdentifiers: IdentifierArchive,
            archivedRoles      : PersonRoleArchive
    ]

    static constraints = {
        timeCreated nullable: true // assigned automatically by db trigger
        timeUpdated nullable: true // assigned automatically by db trigger
    }

    static mapping = {
        table name: "Person"
        version false
        id name: 'uid', column: 'uid', generator: 'assigned', sqlType: 'VARCHAR(64)'
        timeCreated column: 'timeCreated', insertable: false, updateable: false
        timeUpdated column: 'timeUpdated', insertable: false, updateable: false
        isLocked column: 'isLocked', sqlType: 'BOOLEAN'
        names cascade: "all-delete-orphan", batchSize: 25
        telephones cascade: "all-delete-orphan", batchSize: 25
        addresses cascade: "all-delete-orphan", batchSize: 25
        emails cascade: "all-delete-orphan", batchSize: 25
        datesOfBirth cascade: "all-delete-orphan", batchSize: 25
        identifiers cascade: "all-delete-orphan", batchSize: 25
        assignedRoles cascade: "all-delete-orphan", batchSize: 25
        trackStatuses cascade: "all-delete-orphan", batchSize: 25
        delegations cascade: "all-delete-orphan", batchSize: 25
        downstreamObjects cascade: "all-delete-orphan", batchSize: 25
        jobAppointments cascade: "all-delete-orphan", batchSize: 25
        // archivedIdentifiers is read-only
        archivedIdentifiers batchSize: 25
        archivedRoles cascade: "all-delete-orphan", batchSize: 25
    }

    static transients = ['uid']

    public String getUid() { return id }

    public void setUid(String uid) { this.id = uid }

    protected void validatedAssignedRoles() {
        assignedRoles?.each { role ->
            if (archivedRoles?.any { it.roleAsgnUniquePerCat && it.roleCategoryId == role.roleCategoryId }) {
                throw new GrailsRuntimeException("Can't have role ${role.role.roleName} as an assignedRole because a role with the same roleCategory exists as an archivedRole.  Remove the role with roleCategoryId=${role.roleCategoryId} from archiveRoles first, using removeFromArchivedRoles().")
            }

            if (archivedRoles?.any { it.roleId == role.roleId }) {
                throw new GrailsRuntimeException("Can't have role ${role.role.roleName} as an assignedRole because a role with the same roleId exists as an archivedRole.  Remove the role with roleId=${role.roleId} from archiveRoles first, using removeFromArchivedRoles().")
            }
        }
    }

    protected void validatedArchivedRoles() {
        archivedRoles?.each { role ->
            if (assignedRoles?.any { it.roleAsgnUniquePerCat && it.roleCategoryId == role.roleCategoryId }) {
                throw new GrailsRuntimeException("Can't have role ${role.role.roleName} as an archivedRole because a role with the same roleCategory exists as an assignedRole.  Remove the role with roleCategoryId=${role.roleCategoryId} from assignedRoles first, using removeFromAssignedRoles().")
            }

            if (assignedRoles?.any { it.roleId == role.roleId }) {
                throw new GrailsRuntimeException("Can't have role ${role.role.roleName} as an archivedRole because a role with the same roleId exists as an assignedRole.  Remove the role with roleId=${role.roleId} from assignedRoles first, using removeFromAssignedRoles().")
            }
        }
    }

    def beforeValidate() {
        validatedArchivedRoles()
        validatedAssignedRoles()
    }
}
