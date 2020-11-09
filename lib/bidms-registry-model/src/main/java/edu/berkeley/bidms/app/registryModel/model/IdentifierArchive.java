/*
 * Copyright (c) 2016, Regents of the University of California and
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import edu.berkeley.bidms.registryModel.util.EntityUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * An archived identifier.  An example of when an identifier may be archived
 * is when a {@link SORObject} that contains the identifier is deleted.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uid", "person"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "originalSorObjectId", "identifierTypeId"}))
@Entity
public class IdentifierArchive implements Comparable<IdentifierArchive> {

    protected IdentifierArchive() {
    }

    public IdentifierArchive(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @Column(unique = true, insertable = false, updatable = false)
    @Id
    private Long originalIdentifierId;

    @Size(max = 64)
    @Column(length = 64, insertable = false, updatable = false)
    private String uid;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false, insertable = false, updatable = false)
    private Person person;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "identifierTypeId", nullable = false, insertable = false, updatable = false)
    private IdentifierType identifierType;

    @NotNull
    @Column(nullable = false, unique = true, insertable = false, updatable = false)
    private Long originalSorObjectId; // SORObject could have been deleted so don't try to join it

    @Size(max = 64)
    @NotNull
    @Column(nullable = false, length = 64, insertable = false, updatable = false)
    private String identifier;

    @Column(insertable = false, updatable = false)
    private boolean wasActive;

    @Column(insertable = false, updatable = false)
    private boolean wasPrimary;

    @Column(insertable = false, updatable = false)
    private int oldWeight;

    /**
     * IdentifierArchive is a read-only table updated by DB triggers, but
     * tests may override this method to do nothing so that mock data can be
     * written.  The default implementation throws a RuntimeException.
     */
    protected void enforceReadOnly() {
        throw new RuntimeException("IdentifierArchive is read-only");
    }

    // PreRemoval is not present to allow for deletion of Person objects
    @PrePersist
    @PreUpdate
    protected void beforeSave() {
        enforceReadOnly();
    }

    private static final int HCB_INIT_ODDRAND = -590947715;
    private static final int HCB_MULT_ODDRAND = -2080518605;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, identifierType, originalSorObjectId, identifier,
                wasActive, wasPrimary, oldWeight
        };
    }

    @Override
    public int hashCode() {
        return EntityUtil.genHashCode(
                HCB_INIT_ODDRAND, HCB_MULT_ODDRAND,
                getHashCodeObjects()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IdentifierArchive) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((IdentifierArchive) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(IdentifierArchive obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((IdentifierArchive) obj).getHashCodeObjects());
    }

    private void notifyPerson() {
        if (person != null) {
            person.notifyChange(person.getArchivedIdentifiers());
        }
    }

    public Long getId() {
        return originalIdentifierId;
    }

    public void setId(Long id) {
        this.originalIdentifierId = id;
    }

    public Person getPerson() {
        return person;
    }

    void setPerson(Person person) {
        boolean changed = !Objects.equals(person, this.person) || (person != null && !Objects.equals(person.getUid(), uid));
        Person originalPerson = this.person;
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
        if (changed) {
            notifyPerson();
            if (originalPerson != null) {
                originalPerson.notifyChange(originalPerson.getArchivedIdentifiers());
            }
        }
    }

    public IdentifierType getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(IdentifierType identifierType) {
        boolean changed = !Objects.equals(identifierType, this.identifierType);
        this.identifierType = identifierType;
        if (changed) notifyPerson();
    }

    public Long getOriginalSorObjectId() {
        return originalSorObjectId;
    }

    public void setOriginalSorObjectId(Long originalSorObjectId) {
        boolean changed = !Objects.equals(originalSorObjectId, this.originalSorObjectId);
        this.originalSorObjectId = originalSorObjectId;
        if (changed) notifyPerson();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        boolean changed = !Objects.equals(identifier, this.identifier);
        this.identifier = identifier;
        if (changed) notifyPerson();
    }

    public boolean getWasActive() {
        return wasActive;
    }

    public void setWasActive(boolean wasActive) {
        boolean changed = !Objects.equals(wasActive, this.wasActive);
        this.wasActive = wasActive;
        if (changed) notifyPerson();
    }

    public boolean getWasPrimary() {
        return wasPrimary;
    }

    public void setWasPrimary(boolean wasPrimary) {
        boolean changed = !Objects.equals(wasPrimary, this.wasPrimary);
        this.wasPrimary = wasPrimary;
        if (changed) notifyPerson();
    }

    public int getOldWeight() {
        return oldWeight;
    }

    public void setOldWeight(int oldWeight) {
        boolean changed = !Objects.equals(oldWeight, this.oldWeight);
        this.oldWeight = oldWeight;
        if (changed) notifyPerson();
    }
}
