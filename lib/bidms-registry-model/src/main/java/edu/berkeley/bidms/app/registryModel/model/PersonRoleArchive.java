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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.berkeley.bidms.app.registryModel.model.validator.PersonRoleArchiveOnFlushValidator;
import edu.berkeley.bidms.app.registryModel.model.validator.PersonRoleArchiveOnLoadValidator;
import edu.berkeley.bidms.orm.event.ValidateOnFlush;
import edu.berkeley.bidms.orm.event.ValidateOnLoad;
import edu.berkeley.bidms.registryModel.util.DateUtil;
import edu.berkeley.bidms.registryModel.util.EntityUtil;
import org.springframework.validation.Validator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.Objects;

/**
 * An archived role belonging to a person.  This is a role assignment that
 * was active at one point in the past but has gone inactive and has been
 * archived.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uid", "person", "roleCategory", "roleAsgnUniquePerCat", "endOfRoleGraceTimeUseOverrideIfLater"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "roleid"}))
@Entity
public class PersonRoleArchive implements Comparable<PersonRoleArchive>, ValidateOnLoad, ValidateOnFlush {

    protected PersonRoleArchive() {
    }

    public PersonRoleArchive(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PersonRoleArchive_seqgen")
    @SequenceGenerator(name = "PersonRoleArchive_seqgen", sequenceName = "PersonRoleArchive_seq", allocationSize = 1)
    @Id
    private Long id;

    @Size(max = 64)
    @Column(length = 64, insertable = false, updatable = false)
    private String uid;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private Person person;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleCategoryId", nullable = false/*, foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY(roleCategoryId, roleAsgnUniquePerCat) REFERENCES AssignableRoleCategory(id, roleAsgnUniquePerCat)")*/)
    private AssignableRoleCategory roleCategory;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleId", nullable = false/*, foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY(roleId, roleCategoryId) REFERENCES AssignableRole(id, roleCategoryId)")*/)
    private AssignableRole role;

    @Column
    private Long originalPersonRoleId;

    @Column
    private boolean roleAsgnUniquePerCat;

    @NotNull
    @Column(nullable = false)
    private Date startOfRoleGraceTime;

    @Column
    private Date endOfRoleGraceTime;

    // endOfRoleGraceTimeOverride is manually set and overrides the
    // automatically calculated endOfRoleGraceTime
    @Column
    private Date endOfRoleGraceTimeOverride;

    @NotNull
    @Column(nullable = false)
    private Date originalTimeCreated;

    @NotNull
    @Column(nullable = false)
    private Date originalTimeUpdated;

    @Column
    private boolean roleInGrace;

    @Column
    private boolean rolePostGrace;

    @Column(insertable = false, updatable = false)
    private Date timeCreated;

    @Column(insertable = false, updatable = false)
    private Date timeUpdated;

    // There is also a unique constraint in the DB using a partial index
    // "ON PersonRoleArchive(uid, roleCategoryId) WHERE
    // roleAsgnUniquePerCat = true".

    @Override
    public Validator getValidatorForLoad() {
        // will flip the in-grace/post-grace flags if necessary upon load
        return new PersonRoleArchiveOnLoadValidator();
    }

    @Override
    public Validator getValidatorForFlush() {
        return new PersonRoleArchiveOnFlushValidator();
    }

    /**
     * @return endOfRoleGraceTimeOverride if it has a value AND is later than
     * endOfRoleGraceTime, otherwise will return endOfRoleGraceTime
     */
    @Transient
    public Date getEndOfRoleGraceTimeUseOverrideIfLater() {
        // CNR-1215: Decided that override should only be used if it not
        // only exists, but is later than endOfRoleGraceTime.
        return DateUtil.greaterThan(endOfRoleGraceTimeOverride, endOfRoleGraceTime) ? endOfRoleGraceTimeOverride : endOfRoleGraceTime;
    }

    private static final int HCB_INIT_ODDRAND = 579926191;
    private static final int HCB_MULT_ODDRAND = 1431713389;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, role, startOfRoleGraceTime, endOfRoleGraceTime,
                endOfRoleGraceTimeOverride, originalTimeCreated,
                originalTimeUpdated, roleInGrace, rolePostGrace
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
        if (obj instanceof PersonRoleArchive) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((PersonRoleArchive) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(PersonRoleArchive obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((PersonRoleArchive) obj).getHashCodeObjects());
    }

    private void notifyPerson() {
        if (person != null) {
            person.notifyChange(person.getArchivedRoles());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
                originalPerson.notifyChange(originalPerson.getArchivedRoles());
            }
        }
    }

    public AssignableRoleCategory getRoleCategory() {
        return roleCategory;
    }

    public void setRoleCategory(AssignableRoleCategory roleCategory) {
        this.roleCategory = roleCategory;
    }

    public AssignableRole getRole() {
        return role;
    }

    public void setRole(AssignableRole role) {
        boolean changed = !Objects.equals(role, this.role);
        this.role = role;
        if (changed) notifyPerson();
    }

    public Long getOriginalPersonRoleId() {
        return originalPersonRoleId;
    }

    public void setOriginalPersonRoleId(Long originalPersonRoleId) {
        this.originalPersonRoleId = originalPersonRoleId;
    }

    public boolean isRoleAsgnUniquePerCat() {
        return roleAsgnUniquePerCat;
    }

    public void setRoleAsgnUniquePerCat(boolean roleAsgnUniquePerCat) {
        this.roleAsgnUniquePerCat = roleAsgnUniquePerCat;
    }

    public Date getStartOfRoleGraceTime() {
        return startOfRoleGraceTime;
    }

    public void setStartOfRoleGraceTime(Date startOfRoleGraceTime) {
        boolean changed = !Objects.equals(startOfRoleGraceTime, this.startOfRoleGraceTime);
        this.startOfRoleGraceTime = startOfRoleGraceTime;
        if (changed) notifyPerson();
    }

    public Date getEndOfRoleGraceTime() {
        return endOfRoleGraceTime;
    }

    public void setEndOfRoleGraceTime(Date endOfRoleGraceTime) {
        boolean changed = !Objects.equals(endOfRoleGraceTime, this.endOfRoleGraceTime);
        this.endOfRoleGraceTime = endOfRoleGraceTime;
        if (changed) notifyPerson();
    }

    public Date getEndOfRoleGraceTimeOverride() {
        return endOfRoleGraceTimeOverride;
    }

    public void setEndOfRoleGraceTimeOverride(Date endOfRoleGraceTimeOverride) {
        boolean changed = !Objects.equals(endOfRoleGraceTimeOverride, this.endOfRoleGraceTimeOverride);
        this.endOfRoleGraceTimeOverride = endOfRoleGraceTimeOverride;
        if (changed) notifyPerson();
    }

    public Date getOriginalTimeCreated() {
        return originalTimeCreated;
    }

    public void setOriginalTimeCreated(Date originalTimeCreated) {
        boolean changed = !Objects.equals(originalTimeCreated, this.originalTimeCreated);
        this.originalTimeCreated = originalTimeCreated;
        if (changed) notifyPerson();
    }

    public Date getOriginalTimeUpdated() {
        return originalTimeUpdated;
    }

    public void setOriginalTimeUpdated(Date originalTimeUpdated) {
        boolean changed = !Objects.equals(originalTimeUpdated, this.originalTimeUpdated);
        this.originalTimeUpdated = originalTimeUpdated;
        if (changed) notifyPerson();
    }

    public boolean isRoleInGrace() {
        return roleInGrace;
    }

    public void setRoleInGrace(boolean roleInGrace) {
        boolean changed = !Objects.equals(roleInGrace, this.roleInGrace);
        this.roleInGrace = roleInGrace;
        if (changed) notifyPerson();
    }

    public void setRoleInGraceWithoutNotification(boolean roleInGrace) {
        this.roleInGrace = roleInGrace;
    }

    public boolean isRolePostGrace() {
        return rolePostGrace;
    }

    public void setRolePostGrace(boolean rolePostGrace) {
        boolean changed = !Objects.equals(rolePostGrace, this.rolePostGrace);
        this.rolePostGrace = rolePostGrace;
        if (changed) notifyPerson();
    }

    public void setRolePostGraceWithoutNotification(boolean rolePostGrace) {
        this.rolePostGrace = rolePostGrace;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }


    public Date getTimeUpdated() {
        return timeUpdated;
    }
}
