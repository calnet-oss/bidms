/*
 * Copyright (c) 2024, Regents of the University of California and
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

import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

/**
 * A time value that belongs to a person.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uid", "person", "sorObject"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "sorObjectId", "timeTypeId"}))
@Entity
public class PersonTime implements Comparable<PersonTime> {

    protected PersonTime() {
    }

    public PersonTime(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PersonTime_seqgen")
    @SequenceGenerator(name = "PersonTime_seqgen", sequenceName = "PersonTime_seq", allocationSize = 1)
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
    @JoinColumn(name = "timeTypeId", nullable = false)
    private TimeType timeType;

    @Column(insertable = false, updatable = false)
    private Long sorObjectId;

    //@NotNull // TODO in tests
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorObjectId", nullable = false)
    private SORObject sorObject;

    @NotNull
    @Column(nullable = false)
    private Date time;

    @NotNull
    @Column(nullable = false)
    private BigInteger sourceValue;

    private static final int HCB_INIT_ODDRAND = -686965271;
    private static final int HCB_MULT_ODDRAND = 379441719;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, timeType, sorObjectId, time, sourceValue
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
        if (obj instanceof PersonTime) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((PersonTime) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(PersonTime obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((PersonTime) obj).getHashCodeObjects());
    }

    private void notifyPerson() {
        if (person != null) {
            person.notifyChange(person.getTimes());
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
                originalPerson.notifyChange(originalPerson.getTimes());
            }
        }
    }

    public TimeType getTimeType() {
        return timeType;
    }

    public void setTimeType(TimeType timeType) {
        boolean changed = !Objects.equals(timeType, this.timeType);
        this.timeType = timeType;
        if (changed) notifyPerson();
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        boolean changed = !Objects.equals(time, this.time);
        this.time = time;
        if (changed) notifyPerson();
    }

    public BigInteger getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(BigInteger sourceValue) {
        this.sourceValue = sourceValue;
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
}
