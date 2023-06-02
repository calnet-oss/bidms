/*
 * Copyright (c) 2015, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.bidms.app.registryModel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.berkeley.bidms.app.registryModel.model.validator.PersonValidator;
import edu.berkeley.bidms.orm.collection.RebuildableSortedSet;
import edu.berkeley.bidms.orm.collection.RebuildableTreeSet;
import edu.berkeley.bidms.orm.event.ValidateOnFlush;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CollectionType;
import org.springframework.validation.Validator;

import java.util.Date;

/**
 * This is the top-level entity for an identity in the registry.  The
 * registry primary key is {@link #uid} and the uids in other tables are
 * foreign key references to this table.
 * <p>
 * A person has various collections that represent the one to many
 * relationships to person data in other tables.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
public class Person implements ValidateOnFlush {
    @Size(max = 64)
    @Column(length = 64)
    @Id
    private String uid;

    @Column(insertable = false, updatable = false)
    private Date timeCreated;

    @Column(insertable = false, updatable = false)
    private Date timeUpdated;

    @Column
    private boolean isLocked;

    /**
     * We use sorted sets so the sets are ordered the same way each time a
     * person is queried.  This is particularly relevant for JSON generation.
     * We want the JSON output to look the same each time.
     * <p>
     * JPA requires the use of @OrderBy for sorted collection one-to-many
     * joins but this does not determine the ordering in the collection.
     * Instead, the JPA entities implement Comparable.compareTo() and this is
     * what will determine the iteration order.  This gets tricky if
     * attribute values change (because the hash code changes) and this is
     * why you see notifyChange() being called from the child entity setters.
     * When a child setter is called, the person's collection is rebuilt to
     * preserve ordering in the collection after a possible hash code change
     * of the collection element.
     */

    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("id")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.AddressCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<Address> addresses = new RebuildableTreeSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("id")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.PersonNameCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<PersonName> names = new RebuildableTreeSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("id")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.DateOfBirthCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<DateOfBirth> datesOfBirth = new RebuildableTreeSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("id")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.IdentifierCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<Identifier> identifiers = new RebuildableTreeSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("id")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.EmailCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<Email> emails = new RebuildableTreeSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("id")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.TelephoneCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<Telephone> telephones = new RebuildableTreeSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("id")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.PersonRoleCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<PersonRole> assignedRoles = new RebuildableTreeSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("id")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.TrackStatusCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<TrackStatus> trackStatuses = new RebuildableTreeSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("id")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.DelegateProxyCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<DelegateProxy> delegations = new RebuildableTreeSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("id")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.DownstreamObjectCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<DownstreamObject> downstreamObjects = new RebuildableTreeSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "uid", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("id")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.JobAppointmentCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<JobAppointment> jobAppointments = new RebuildableTreeSet<>();

    // archivedIdentifiers is read-only
    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "person")
    //@OrderBy("originalIdentifierId")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.IdentifierArchiveCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<IdentifierArchive> archivedIdentifiers = new RebuildableTreeSet<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("id")
    @CollectionType(type = edu.berkeley.bidms.registryModel.hibernate.usertype.person.PersonRoleArchiveCollectionType.class)
    @JsonDeserialize(as = RebuildableTreeSet.class)
    private RebuildableSortedSet<PersonRoleArchive> archivedRoles = new RebuildableTreeSet<>();

    public Person addToAddresses(Address obj) {
        obj.setPerson(this);
        addresses.add(obj);
        return this;
    }

    public Person removeFromAddresses(Address obj) {
        obj.setPerson(null);
        addresses.remove(obj);
        return this;
    }

    public Person addToNames(PersonName obj) {
        obj.setPerson(this);
        names.add(obj);
        return this;
    }

    public Person removeFromNames(PersonName obj) {
        obj.setPerson(null);
        names.remove(obj);
        return this;
    }

    public Person addToDatesOfBirth(DateOfBirth obj) {
        obj.setPerson(this);
        datesOfBirth.add(obj);
        return this;
    }

    public Person removeFromDatesOfBirth(DateOfBirth obj) {
        obj.setPerson(null);
        datesOfBirth.remove(obj);
        return this;
    }

    public Person addToIdentifiers(Identifier obj) {
        obj.setPerson(this);
        identifiers.add(obj);
        return this;
    }

    public Person removeFromIdentifiers(Identifier obj) {
        obj.setPerson(null);
        identifiers.remove(obj);
        return this;
    }

    public Person addToEmails(Email obj) {
        obj.setPerson(this);
        emails.add(obj);
        return this;
    }

    public Person removeFromEmails(Email obj) {
        obj.setPerson(null);
        emails.remove(obj);
        return this;
    }

    public Person addToTelephones(Telephone obj) {
        obj.setPerson(this);
        telephones.add(obj);
        return this;
    }

    public Person removeFromTelephones(Telephone obj) {
        obj.setPerson(null);
        telephones.remove(obj);
        return this;
    }

    public Person addToAssignedRoles(PersonRole obj) {
        obj.setPerson(this);
        assignedRoles.add(obj);
        return this;
    }

    public Person removeFromAssignedRoles(PersonRole obj) {
        obj.setPerson(null);
        assignedRoles.remove(obj);
        return this;
    }

    public Person addToTrackStatuses(TrackStatus obj) {
        obj.setPerson(this);
        trackStatuses.add(obj);
        return this;
    }

    public Person removeFromTrackStatuses(TrackStatus obj) {
        obj.setPerson(null);
        trackStatuses.remove(obj);
        return this;
    }

    public Person addToDelegations(DelegateProxy obj) {
        obj.setPerson(this);
        delegations.add(obj);
        return this;
    }

    public Person removeFromDelegations(DelegateProxy obj) {
        obj.setPerson(null);
        delegations.remove(obj);
        return this;
    }

    public Person addToDownstreamObjects(DownstreamObject obj) {
        obj.setPerson(this);
        downstreamObjects.add(obj);
        return this;
    }

    public Person removeFromDownstreamObjects(DownstreamObject obj) {
        obj.setPerson(null);
        downstreamObjects.remove(obj);
        return this;
    }

    public Person addToJobAppointments(JobAppointment obj) {
        obj.setPerson(this);
        jobAppointments.add(obj);
        return this;
    }

    public Person removeFromJobAppointments(JobAppointment obj) {
        obj.setPerson(null);
        jobAppointments.remove(obj);
        return this;
    }

    public Person addToArchivedRoles(PersonRoleArchive obj) {
        obj.setPerson(this);
        archivedRoles.add(obj);
        return this;
    }

    public Person removeFromArchivedRoles(PersonRoleArchive obj) {
        obj.setPerson(null);
        archivedRoles.remove(obj);
        return this;
    }

    public boolean safeAddToAddresses(Address obj) {
        obj.setPerson(this);
        return safeAddTo(getAddresses(), obj);
    }

    public boolean safeRemoveFromAddresses(Address obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getAddresses(), obj);
    }

    public boolean safeAddToNames(PersonName obj) {
        obj.setPerson(this);
        return safeAddTo(getNames(), obj);
    }

    public boolean safeRemoveFromNames(PersonName obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getNames(), obj);
    }

    public boolean safeAddToDatesOfBirth(DateOfBirth obj) {
        obj.setPerson(this);
        return safeAddTo(getDatesOfBirth(), obj);
    }

    public boolean safeRemoveFromDatesOfBirth(DateOfBirth obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getDatesOfBirth(), obj);
    }

    public boolean safeAddToIdentifiers(Identifier obj) {
        obj.setPerson(this);
        return safeAddTo(getIdentifiers(), obj);
    }

    public boolean safeRemoveFromIdentifiers(Identifier obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getIdentifiers(), obj);
    }

    public boolean safeAddToEmails(Email obj) {
        obj.setPerson(this);
        return safeAddTo(getEmails(), obj);
    }

    public boolean safeRemoveFromEmails(Email obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getEmails(), obj);
    }

    public boolean safeAddToTelephones(Telephone obj) {
        obj.setPerson(this);
        return safeAddTo(getTelephones(), obj);
    }

    public boolean safeRemoveFromTelephones(Telephone obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getTelephones(), obj);
    }

    public boolean safeAddToAssignedRoles(PersonRole obj) {
        obj.setPerson(this);
        return safeAddTo(getAssignedRoles(), obj);
    }

    public boolean safeRemoveFromAssignedRoles(PersonRole obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getAssignedRoles(), obj);
    }

    public boolean safeAddToTrackStatuses(TrackStatus obj) {
        obj.setPerson(this);
        return safeAddTo(getTrackStatuses(), obj);
    }

    public boolean safeRemoveFromTrackStatuses(TrackStatus obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getTrackStatuses(), obj);
    }

    public boolean safeAddToDelegations(DelegateProxy obj) {
        obj.setPerson(this);
        return safeAddTo(getDelegations(), obj);
    }

    public boolean safeRemoveFromDelegations(DelegateProxy obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getDelegations(), obj);
    }

    public boolean safeAddToDownstreamObjects(DownstreamObject obj) {
        obj.setPerson(this);
        return safeAddTo(getDownstreamObjects(), obj);
    }

    public boolean safeRemoveFromDownstreamObjects(DownstreamObject obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getDownstreamObjects(), obj);
    }

    public boolean safeAddToJobAppointments(JobAppointment obj) {
        obj.setPerson(this);
        return safeAddTo(getJobAppointments(), obj);
    }

    public boolean safeRemoveFromJobAppointments(JobAppointment obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getJobAppointments(), obj);
    }

    public boolean safeAddToArchivedIdentifiers(IdentifierArchive obj) {
        obj.setPerson(this);
        return safeAddTo(getArchivedIdentifiers(), obj);
    }

    public boolean safeRemoveFromArchivedIdentifiers(IdentifierArchive obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getArchivedIdentifiers(), obj);
    }

    public boolean safeAddToArchivedRoles(PersonRoleArchive obj) {
        obj.setPerson(this);
        return safeAddTo(getArchivedRoles(), obj);
    }

    public boolean safeRemoveFromArchivedRoles(PersonRoleArchive obj) {
        obj.setPerson(null);
        return safeRemoveFrom(getArchivedRoles(), obj);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean safeAddTo(RebuildableSortedSet collection, Object obj) {
        //rebuildCollectionSetIfNecessary(collection);
        return collection.add(obj);
    }

    @SuppressWarnings("rawtypes")
    private boolean safeRemoveFrom(RebuildableSortedSet collection, Object obj) {
        //rebuildCollectionSetIfNecessary(collection);
        return collection.remove(obj);
    }

    @SuppressWarnings("rawtypes")
    private void rebuildCollectionSetIfNecessary(RebuildableSortedSet collection) {
        // This will cause the sorted collection to be re-sorted if any of
        // the hash codes have changed.  Relevant because SortedSet.add() and
        // SortedSet.remove() is dependent on proper ordering to find the object.
        collection.rebuild();
    }

    @Override
    public Validator getValidatorForFlush() {
        return new PersonValidator();
    }

    /**
     * Rebuild the collection because the sort order may have changed due to
     * changed attributes of a collection element.
     *
     * @param collection A collection of entities that belongs to this
     *                   person.
     */
    <T extends Comparable> void notifyChange(RebuildableSortedSet<T> collection) {
        // Rebuild the collection because the sort order may have changed
        // due to changed attributes of a collection element.
        collection.rebuild();
    }

    @JsonSetter
    public void setId(String uid) {
        this.uid = uid;
    }

    @JsonIgnore
    public String getId() {
        return uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }

    public Date getTimeUpdated() {
        return timeUpdated;
    }

    public void setTimeUpdated(Date timeUpdated) {
        this.timeUpdated = timeUpdated;
    }

    public boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean locked) {
        this.isLocked = locked;
    }

    public RebuildableSortedSet<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(RebuildableSortedSet<Address> addresses) {
        this.addresses = addresses;
    }

    public RebuildableSortedSet<DateOfBirth> getDatesOfBirth() {
        return datesOfBirth;
    }

    public void setDatesOfBirth(RebuildableSortedSet<DateOfBirth> datesOfBirth) {
        this.datesOfBirth = datesOfBirth;
    }

    public RebuildableSortedSet<PersonName> getNames() {
        return names;
    }

    public void setNames(RebuildableSortedSet<PersonName> names) {
        this.names = names;
    }

    public RebuildableSortedSet<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(RebuildableSortedSet<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public RebuildableSortedSet<Email> getEmails() {
        return emails;
    }

    public void setEmails(RebuildableSortedSet<Email> emails) {
        this.emails = emails;
    }

    public RebuildableSortedSet<Telephone> getTelephones() {
        return telephones;
    }

    public void setTelephones(RebuildableSortedSet<Telephone> telephones) {
        this.telephones = telephones;
    }

    public RebuildableSortedSet<PersonRole> getAssignedRoles() {
        return assignedRoles;
    }

    public void setAssignedRoles(RebuildableSortedSet<PersonRole> assignedRoles) {
        this.assignedRoles = assignedRoles;
    }

    public RebuildableSortedSet<TrackStatus> getTrackStatuses() {
        return trackStatuses;
    }

    public void setTrackStatuses(RebuildableSortedSet<TrackStatus> trackStatuses) {
        this.trackStatuses = trackStatuses;
    }

    public RebuildableSortedSet<DelegateProxy> getDelegations() {
        return delegations;
    }

    public void setDelegations(RebuildableSortedSet<DelegateProxy> delegations) {
        this.delegations = delegations;
    }

    public RebuildableSortedSet<DownstreamObject> getDownstreamObjects() {
        return downstreamObjects;
    }

    public void setDownstreamObjects(RebuildableSortedSet<DownstreamObject> downstreamObjects) {
        this.downstreamObjects = downstreamObjects;
    }

    public RebuildableSortedSet<JobAppointment> getJobAppointments() {
        return jobAppointments;
    }

    public void setJobAppointments(RebuildableSortedSet<JobAppointment> jobAppointments) {
        this.jobAppointments = jobAppointments;
    }

    public RebuildableSortedSet<IdentifierArchive> getArchivedIdentifiers() {
        return archivedIdentifiers;
    }

    public void setArchivedIdentifiers(RebuildableSortedSet<IdentifierArchive> archivedIdentifiers) {
        this.archivedIdentifiers = archivedIdentifiers;
    }

    public RebuildableSortedSet<PersonRoleArchive> getArchivedRoles() {
        return archivedRoles;
    }

    public void setArchivedRoles(RebuildableSortedSet<PersonRoleArchive> archivedRoles) {
        this.archivedRoles = archivedRoles;
    }
}
