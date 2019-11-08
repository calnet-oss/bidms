/*
 * Copyright (c) 2019, Regents of the University of California and
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

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.berkeley.bidms.app.registryModel.model.validator.PersonValidator;
import edu.berkeley.bidms.registryModel.hibernate.usertype.RegistrySortedSetType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.SortedSet;

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
public class Person {
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
     * We want the JSON output to look the same each time. Instantiating the
     * set is necessary when using RegistrySortedSetType.
     * <p>
     * JPA requires the use of @OrderBy for sorted collection one-to-many
     * joins but this does not determine the ordering in the
     * RegistrySortedSetType collection.  Instead, the JPA entities implement
     * Comparable.compareTo() and this is what will determine the iteration
     * order.  This gets tricky if attribute values change (because the hash
     * code changes) and this is why you see notifyChange() being called from
     * the child entity setters.  When a child setter is called, the person's
     * collection is rebuilt to preserve ordering in the collection after a
     * possible hash code change of the collection element.
     */

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private SortedSet<Address> addresses = RegistrySortedSetType.newSet(Address.class);

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private SortedSet<PersonName> names = RegistrySortedSetType.newSet(PersonName.class);

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private SortedSet<DateOfBirth> datesOfBirth = RegistrySortedSetType.newSet(DateOfBirth.class);

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private SortedSet<Identifier> identifiers = RegistrySortedSetType.newSet(Identifier.class);

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private SortedSet<Email> emails = RegistrySortedSetType.newSet(Email.class);

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private SortedSet<Telephone> telephones = RegistrySortedSetType.newSet(Telephone.class);

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private SortedSet<PersonRole> assignedRoles = RegistrySortedSetType.newSet(PersonRole.class);

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private SortedSet<TrackStatus> trackStatuses = RegistrySortedSetType.newSet(TrackStatus.class);

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private SortedSet<DelegateProxy> delegations = RegistrySortedSetType.newSet(DelegateProxy.class);

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private SortedSet<DownstreamObject> downstreamObjects = RegistrySortedSetType.newSet(DownstreamObject.class);

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private SortedSet<JobAppointment> jobAppointments = RegistrySortedSetType.newSet(JobAppointment.class);

    // archivedIdentifiers is read-only
    @OneToMany(mappedBy = "person")
    @OrderBy("originalIdentifierId")
    private SortedSet<IdentifierArchive> archivedIdentifiers = RegistrySortedSetType.newSet(IdentifierArchive.class);

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private SortedSet<PersonRoleArchive> archivedRoles = RegistrySortedSetType.newSet(PersonRoleArchive.class);

    public Person addToAddresses(Address obj) {
        obj.setPerson(this);
        addresses.add(obj);
        return this;
    }

    public Person removeFromAddresses(Address obj) {
        if (addresses.remove(obj)) {
            obj.setPerson(null);
        }
        return this;
    }

    public Person addToNames(PersonName obj) {
        obj.setPerson(this);
        names.add(obj);
        return this;
    }

    public Person removeFromNames(PersonName obj) {
        if (names.remove(obj)) {
            obj.setPerson(null);
        }
        return this;
    }

    public Person addToDatesOfBirth(DateOfBirth obj) {
        obj.setPerson(this);
        datesOfBirth.add(obj);
        return this;
    }

    public Person removeFromDatesOfBirth(DateOfBirth obj) {
        if (datesOfBirth.remove(obj)) {
            obj.setPerson(null);
        }
        return this;
    }

    public Person addToIdentifiers(Identifier obj) {
        obj.setPerson(this);
        identifiers.add(obj);
        return this;
    }

    public Person removeFromIdentifiers(Identifier obj) {
        if (identifiers.remove(obj)) {
            obj.setPerson(null);
        }
        return this;
    }

    public Person addToEmails(Email obj) {
        obj.setPerson(this);
        emails.add(obj);
        return this;
    }

    public Person removeFromEmails(Email obj) {
        if (emails.remove(obj)) {
            obj.setPerson(null);
        }
        return this;
    }

    public Person addToTelephones(Telephone obj) {
        obj.setPerson(this);
        telephones.add(obj);
        return this;
    }

    public Person removeFromTelephones(Telephone obj) {
        if (telephones.remove(obj)) {
            obj.setPerson(null);
        }
        return this;
    }

    public Person addToAssignedRoles(PersonRole obj) {
        obj.setPerson(this);
        assignedRoles.add(obj);
        doValidation();
        return this;
    }

    public Person removeFromAssignedRoles(PersonRole obj) {
        if (assignedRoles.remove(obj)) {
            obj.setPerson(null);
        }
        doValidation();
        return this;
    }

    public Person addToTrackStatuses(TrackStatus obj) {
        obj.setPerson(this);
        trackStatuses.add(obj);
        return this;
    }

    public Person removeFromTrackStatuses(TrackStatus obj) {
        if (trackStatuses.remove(obj)) {
            obj.setPerson(null);
        }
        return this;
    }

    public Person addToDelegations(DelegateProxy obj) {
        obj.setPerson(this);
        delegations.add(obj);
        return this;
    }

    public Person removeFromDelegations(DelegateProxy obj) {
        if (delegations.remove(obj)) {
            obj.setPerson(null);
        }
        return this;
    }

    public Person addToDownstreamObjects(DownstreamObject obj) {
        obj.setPerson(this);
        downstreamObjects.add(obj);
        return this;
    }

    public Person removeFromDownstreamObjects(DownstreamObject obj) {
        if (downstreamObjects.remove(obj)) {
            obj.setPerson(null);
        }
        return this;
    }

    public Person addToJobAppointments(JobAppointment obj) {
        obj.setPerson(this);
        jobAppointments.add(obj);
        return this;
    }

    public Person removeFromJobAppointments(JobAppointment obj) {
        if (jobAppointments.remove(obj)) {
            obj.setPerson(null);
        }
        return this;
    }

    public Person addToArchivedRoles(PersonRoleArchive obj) {
        obj.setPerson(this);
        archivedRoles.add(obj);
        doValidation();
        return this;
    }

    public Person removeFromArchivedRoles(PersonRoleArchive obj) {
        if (archivedRoles.remove(obj)) {
            obj.setPerson(null);
        }
        doValidation();
        return this;
    }

    /**
     * It's possible that a role is in the archive and it has switched from
     * in-grace to post-grace based on the end grace date, but the quartz job
     * hasn't had a chance yet to flip this row to isPostGrace=true.  So we
     * do the check here and do that flipping here, otherwise we will
     * encounter a validation error when the person is saved.
     */
    @PostLoad
    @PreUpdate
    @PrePersist
    protected void doValidation() {
        DataBinder binder = new DataBinder(this);
        binder.setValidator(new PersonValidator());
        binder.validate();
        BindingResult result = binder.getBindingResult();
        if (result.hasErrors()) {
            throw new RuntimeException("person " + getUid() + " did not validate: " + result.getAllErrors().get(0).toString());
        }

        // Hash codes may have changed due to grace flag changes.
        // (TODO: I don't think this is necessary anymore - notify done in the flag setters.)
        //notifyChange(archivedRoles);
        //notifyChange(assignedRoles);
    }

    /**
     * Rebuild the collection because the sort order may have changed due to
     * changed attributes of a collection element.
     *
     * @param collection A collection of entities that belongs to this
     *                   person.
     */
    <T extends Comparable> void notifyChange(SortedSet<T> collection) {
        // Rebuild the collection because the sort order may have changed
        // due to changed attributes of a collection element.
        Collection<T> cloned = new ArrayList<>(collection);
        collection.clear();
        collection.addAll(cloned);
    }

    public void setId(String uid) {
        this.uid = uid;
    }

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

    public SortedSet<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(SortedSet<Address> addresses) {
        this.addresses = addresses;
    }

    public SortedSet<DateOfBirth> getDatesOfBirth() {
        return datesOfBirth;
    }

    public void setDatesOfBirth(SortedSet<DateOfBirth> datesOfBirth) {
        this.datesOfBirth = datesOfBirth;
    }

    public SortedSet<PersonName> getNames() {
        return names;
    }

    public void setNames(SortedSet<PersonName> names) {
        this.names = names;
    }

    public SortedSet<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(SortedSet<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public SortedSet<Email> getEmails() {
        return emails;
    }

    public void setEmails(SortedSet<Email> emails) {
        this.emails = emails;
    }

    public SortedSet<Telephone> getTelephones() {
        return telephones;
    }

    public void setTelephones(SortedSet<Telephone> telephones) {
        this.telephones = telephones;
    }

    public SortedSet<PersonRole> getAssignedRoles() {
        return assignedRoles;
    }

    public void setAssignedRoles(SortedSet<PersonRole> assignedRoles) {
        this.assignedRoles = assignedRoles;
    }

    public SortedSet<TrackStatus> getTrackStatuses() {
        return trackStatuses;
    }

    public void setTrackStatuses(SortedSet<TrackStatus> trackStatuses) {
        this.trackStatuses = trackStatuses;
    }

    public SortedSet<DelegateProxy> getDelegations() {
        return delegations;
    }

    public void setDelegations(SortedSet<DelegateProxy> delegations) {
        this.delegations = delegations;
    }

    public SortedSet<DownstreamObject> getDownstreamObjects() {
        return downstreamObjects;
    }

    public void setDownstreamObjects(SortedSet<DownstreamObject> downstreamObjects) {
        this.downstreamObjects = downstreamObjects;
    }

    public SortedSet<JobAppointment> getJobAppointments() {
        return jobAppointments;
    }

    public void setJobAppointments(SortedSet<JobAppointment> jobAppointments) {
        this.jobAppointments = jobAppointments;
    }

    public SortedSet<IdentifierArchive> getArchivedIdentifiers() {
        return archivedIdentifiers;
    }

    public void setArchivedIdentifiers(SortedSet<IdentifierArchive> archivedIdentifiers) {
        this.archivedIdentifiers = archivedIdentifiers;
    }

    public SortedSet<PersonRoleArchive> getArchivedRoles() {
        return archivedRoles;
    }

    public void setArchivedRoles(SortedSet<PersonRoleArchive> archivedRoles) {
        this.archivedRoles = archivedRoles;
    }
}
