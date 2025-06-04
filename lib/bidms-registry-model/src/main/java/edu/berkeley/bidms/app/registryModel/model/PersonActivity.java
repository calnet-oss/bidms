/*
 * Copyright (c) 2025, Regents of the University of California and
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
import edu.berkeley.bidms.registryModel.util.EntityUtil;
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
 * An activity that belongs to a person.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uid", "person", "sorObject"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "sorObjectId", "activityTypeId", "sourceActivityId"}))
@Entity
public class PersonActivity implements Comparable<PersonActivity> {

    protected PersonActivity() {
    }

    public PersonActivity(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PersonActivity_seqgen")
    @SequenceGenerator(name = "PersonActivity_seqgen", sequenceName = "PersonActivity_seq", allocationSize = 1)
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
    @JoinColumn(name = "activityTypeId", nullable = false)
    private ActivityType activityType;

    @Column(insertable = false, updatable = false)
    private Long sorObjectId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorObjectId", nullable = false)
    private SORObject sorObject;

    @NotNull
    @Size(max = 31)
    @Column(length = 31)
    private String sourceActivityId;

    @Size(max = 31)
    @Column(length = 31)
    private String sourceActivityCode;

    @Column
    private Date lastAttemptTime;

    @Column
    private Date lastCompletionTime;

    @Size(max = 63)
    @Column(length = 63)
    private String activityStatus;

    private static final int HCB_INIT_ODDRAND = 393828601;
    private static final int HCB_MULT_ODDRAND = -1463611535;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, activityType, sorObjectId, sourceActivityId,
                sourceActivityCode, lastAttemptTime,
                lastCompletionTime, activityStatus
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
        if (obj instanceof PersonActivity) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((PersonActivity) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(PersonActivity obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((PersonActivity) obj).getHashCodeObjects());
    }

    private void notifyPerson() {
        if (person != null) {
            person.notifyChange(person.getActivities());
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
                originalPerson.notifyChange(originalPerson.getActivities());
            }
        }
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        boolean changed = !Objects.equals(activityType, this.activityType);
        this.activityType = activityType;
        if (changed) notifyPerson();
    }

    @Transient
    public Long getSorObjectId() {
        return sorObjectId;
    }

    public SORObject getSorObject() {
        return sorObject;
    }

    public void setSorObject(SORObject sorObject) {
        boolean changed = !Objects.equals(sorObject, this.sorObject) || (sorObject != null && !Objects.equals(sorObject.getId(), sorObjectId));
        this.sorObject = sorObject;
        this.sorObjectId = sorObject != null ? sorObject.getId() : null;
        if (changed) notifyPerson();
    }

    public String getSourceActivityId() {
        return sourceActivityId;
    }

    public void setSourceActivityId(String sourceActivityId) {
        boolean changed = !Objects.equals(sourceActivityId, this.sourceActivityId);
        this.sourceActivityId = sourceActivityId;
        if (changed) notifyPerson();
    }

    public String getSourceActivityCode() {
        return sourceActivityCode;
    }

    public void setSourceActivityCode(String sourceActivityCode) {
        boolean changed = !Objects.equals(sourceActivityCode, this.sourceActivityCode);
        this.sourceActivityCode = sourceActivityCode;
        if (changed) notifyPerson();
    }

    public Date getLastAttemptTime() {
        return lastAttemptTime;
    }

    public void setLastAttemptTime(Date lastAttemptTime) {
        boolean changed = !Objects.equals(lastAttemptTime, this.lastAttemptTime);
        this.lastAttemptTime = lastAttemptTime;
        if (changed) notifyPerson();
    }

    public Date getLastCompletionTime() {
        return lastCompletionTime;
    }

    public void setLastCompletionTime(Date lastCompletionTime) {
        boolean changed = !Objects.equals(lastCompletionTime, this.lastCompletionTime);
        this.lastCompletionTime = lastCompletionTime;
        if (changed) notifyPerson();
    }

    public String getActivityStatus() {
        return activityStatus;
    }

    public void setActivityStatus(String activityStatus) {
        boolean changed = !Objects.equals(activityStatus, this.activityStatus);
        this.activityStatus = activityStatus;
        if (changed) notifyPerson();
    }
}
