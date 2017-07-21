package edu.berkeley.registry.model

import edu.berkeley.hibernate.usertype.RegistrySortedSetType
import org.grails.core.exceptions.GrailsRuntimeException

class Person {

    String uid
    Date timeCreated
    Date timeUpdated
    boolean isLocked

    // We use sorted sets so the sets are ordered the same way each time a
    // person is queried.  This is particularly relevant for JSON
    // generation.  We want the JSON output to look the same each time.
    // Instantiating the set is necessary when using RegistrySortedSetType.
    SortedSet<Address> addresses = RegistrySortedSetType.newSet(Address)
    SortedSet<PersonName> names = RegistrySortedSetType.newSet(PersonName)
    SortedSet<DateOfBirth> datesOfBirth = RegistrySortedSetType.newSet(DateOfBirth)
    SortedSet<Identifier> identifiers = RegistrySortedSetType.newSet(Identifier)
    SortedSet<Email> emails = RegistrySortedSetType.newSet(Email)
    SortedSet<Telephone> telephones = RegistrySortedSetType.newSet(Telephone)
    SortedSet<PersonRole> assignedRoles = RegistrySortedSetType.newSet(PersonRole)
    SortedSet<TrackStatus> trackStatuses = RegistrySortedSetType.newSet(TrackStatus)
    SortedSet<DelegateProxy> delegations = RegistrySortedSetType.newSet(DelegateProxy)
    SortedSet<DownstreamObject> downstreamObjects = RegistrySortedSetType.newSet(DownstreamObject)
    SortedSet<JobAppointment> jobAppointments = RegistrySortedSetType.newSet(JobAppointment)
    SortedSet<IdentifierArchive> archivedIdentifiers = RegistrySortedSetType.newSet(IdentifierArchive)
    SortedSet<PersonRoleArchive> archivedRoles = RegistrySortedSetType.newSet(PersonRoleArchive)

    Person safeRemoveFromAddresses(Address obj) { return safeRemoveFrom("addresses", obj) }

    Person safeRemoveFromNames(PersonName obj) { return safeRemoveFrom("names", obj) }

    Person safeRemoveFromDatesOfBirth(DateOfBirth obj) { return safeRemoveFrom("datesOfBirth", obj) }

    Person safeRemoveFromIdentifiers(Identifier obj) { return safeRemoveFrom("identifiers", obj) }

    Person safeRemoveFromEmails(Email obj) { return safeRemoveFrom("emails", obj) }

    Person safeRemoveFromTelephones(Telephone obj) { return safeRemoveFrom("telephones", obj) }

    Person safeRemoveFromAssignedRoles(PersonRole obj) { return safeRemoveFrom("assignedRoles", obj) }

    Person safeRemoveFromTrackStatuses(TrackStatus obj) { return safeRemoveFrom("trackStatuses", obj) }

    Person safeRemoveFromDelegations(DelegateProxy obj) { return safeRemoveFrom("delegations", obj) }

    Person safeRemoveFromDownstreamObjects(DownstreamObject obj) { return safeRemoveFrom("downstreamObjects", obj) }

    Person safeRemoveFromJobAppointments(JobAppointment obj) { return safeRemoveFrom("jobAppointments", obj) }

    Person safeRemoveFromArchivedIdentifiers(IdentifierArchive obj) { return safeRemoveFrom("archivedIdentifiers", obj) }

    Person safeRemoveFromArchivedRoles(PersonRoleArchive obj) { return safeRemoveFrom("archivedRoles", obj) }

    Person safeRemoveFrom(String collectionPropertyName, Object obj) {
        // This will cause the sorted collection to be re-sorted if any of
        // the hash codes have changed.  Relevant because SortedSet.remove()
        // is dependent on proper ordering to find the object.
        SortedSet collection = (SortedSet) getProperty(collectionPropertyName)
        Collection cloned = (collection ? new ArrayList(collection) : null)
        cloned?.each { it.hashCode() }

        return removeFrom(collectionPropertyName, obj)
    }

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
        id column: 'uid', name: 'uid', generator: 'assigned', sqlType: 'VARCHAR(64)'
        timeCreated column: 'timeCreated', insertable: false, updateable: false
        timeUpdated column: 'timeUpdated', insertable: false, updateable: false
        isLocked column: 'isLocked', sqlType: 'BOOLEAN'
        names cascade: "all-delete-orphan", batchSize: 25, type: RegistrySortedSetType
        telephones cascade: "all-delete-orphan", batchSize: 25, type: RegistrySortedSetType
        addresses cascade: "all-delete-orphan", batchSize: 25, type: RegistrySortedSetType
        emails cascade: "all-delete-orphan", batchSize: 25, type: RegistrySortedSetType
        datesOfBirth cascade: "all-delete-orphan", batchSize: 25, type: RegistrySortedSetType
        identifiers cascade: "all-delete-orphan", batchSize: 25, type: RegistrySortedSetType
        assignedRoles cascade: "all-delete-orphan", batchSize: 25, type: RegistrySortedSetType
        trackStatuses cascade: "all-delete-orphan", batchSize: 25, type: RegistrySortedSetType
        delegations cascade: "all-delete-orphan", batchSize: 25, type: RegistrySortedSetType
        downstreamObjects cascade: "all-delete-orphan", batchSize: 25, type: RegistrySortedSetType
        jobAppointments cascade: "all-delete-orphan", batchSize: 25, type: RegistrySortedSetType
        // archivedIdentifiers is read-only
        archivedIdentifiers batchSize: 25, type: RegistrySortedSetType
        archivedRoles cascade: "all-delete-orphan", batchSize: 25, type: RegistrySortedSetType
    }

    protected void validatedAssignedRoles() {
        assignedRoles?.each { role ->
            if (archivedRoles?.any { it.roleAsgnUniquePerCat && it.roleCategoryId == role.roleCategoryId }) {
                throw new GrailsRuntimeException("Uid $uid can't have role ${role.role.roleName} as an assignedRole because a role with the same roleCategory exists as an archivedRole.  Remove the role with roleCategoryId=${role.roleCategoryId} from archiveRoles first, using removeFromArchivedRoles().")
            }
        }

        // the second iteration on the same collection is on purpose so that
        // we consistently fail on any unique-only categories before we fail
        // on any roleIds
        assignedRoles?.each { role ->
            if (archivedRoles?.any { it.roleId == role.roleId }) {
                throw new GrailsRuntimeException("Uid $uid can't have role ${role.role.roleName} as an assignedRole because a role with the same roleId exists as an archivedRole.  Remove the role with roleId=${role.roleId} from archiveRoles first, using removeFromArchivedRoles().")
            }
        }
    }

    protected void validatedArchivedRoles() {
        Date currentTime = new Date()
        archivedRoles?.each { role ->
            if (assignedRoles?.any { it.roleAsgnUniquePerCat && it.roleCategoryId == role.roleCategoryId }) {
                throw new GrailsRuntimeException("Uid $uid can't have role ${role.role.roleName} as an archivedRole because a role with the same roleCategory exists as an assignedRole.  Remove the role with roleCategoryId=${role.roleCategoryId} from assignedRoles first, using removeFromAssignedRoles().")
            }

            // Flip the in-grace/post-grace booleans, if need be. 
            // Necessary, otherwise validation errors could happen when
            // saving the person.
            resetArchivedRoleFlags(currentTime, role)
        }

        // the second iteration on the same collection is on purpose so that
        // we consistently fail on any unique-only categories before we fail
        // on any roleIds
        archivedRoles?.each { role ->
            if (assignedRoles?.any { it.roleId == role.roleId }) {
                throw new GrailsRuntimeException("Uid $uid can't have role ${role.role.roleName} as an archivedRole because a role with the same roleId exists as an assignedRole.  Remove the role with roleId=${role.roleId} from assignedRoles first, using removeFromAssignedRoles().")
            }
        }
    }

    /**
     * It's possible that a role is in the archive and it has switched
     * from in-grace to post-grace based on the end grace date, but the
     * quartz job hasn't had a chance yet to flip this row to
     * isPostGrace=true.  So we do the check here and do that flipping
     * here, otherwise we will encounter a validation error when the
     * person is saved.
     */
    static void resetArchivedRoleFlags(Date currentTime, PersonRoleArchive archivedRole) {
        archivedRole.roleInGrace = (archivedRole.endOfRoleGraceTimeUseOverrideIfLater ? currentTime >= archivedRole.startOfRoleGraceTime && currentTime < archivedRole.endOfRoleGraceTimeUseOverrideIfLater : true)
        archivedRole.rolePostGrace = (archivedRole.endOfRoleGraceTimeUseOverrideIfLater ? currentTime >= archivedRole.endOfRoleGraceTimeUseOverrideIfLater : false)
    }

    def beforeValidate() {
        validatedArchivedRoles()
        validatedAssignedRoles()

        // Recalculate the hash codes because changing grace flags changes
        // the hash code and the hashCodeChangeCallback needs to be invoked
        // here before Hibernate does its save.
        archivedRoles?.each { it.hashCode() }
        assignedRoles?.each { it.hashCode() }
    }

    void setId(String uid) {
        this.uid = uid
    }

    String getId() {
        return uid
    }

    /**
     * @deprecated Use findByUid() or get() instead.
     */
    @Deprecated
    static Person findById(String id) {
        return findByUid(id)
    }
}
