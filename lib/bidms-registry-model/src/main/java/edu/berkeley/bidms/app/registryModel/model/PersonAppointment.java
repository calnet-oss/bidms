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

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.berkeley.bidms.app.registryModel.model.compositeKey.PersonAppointmentCompositeKey;
import edu.berkeley.bidms.registryModel.util.EntityUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Objects;

/**
 * A base for any kind of appointment.  An appointment is defined as
 * something that lasts for a period of time.  An example implementation of
 * this is a {@link JobAppointment} that represents a HR or payroll job. Even
 * though it's a period of time, the dates may not always be known
 * (open-ended appointment).  Both {@link #beginDate} and {@link #endDate}
 * are nullable.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@IdClass(PersonAppointmentCompositeKey.class)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "sorObjectId", "apptTypeId", "apptIdentifier"}))
@Inheritance(strategy = InheritanceType.JOINED)
public class PersonAppointment implements Comparable<PersonAppointment> {

    protected PersonAppointment() {
    }

    public PersonAppointment(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PersonAppointment_seqgen")
    @SequenceGenerator(name = "PersonAppointment_seqgen", sequenceName = "personappointment_seq", allocationSize = 1)
    @Id
    private Long id;

    @Size(max = 64)
    @NotNull
    @Column(length = 64, nullable = false)
    private String uid;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false, insertable = false, updatable = false)
    @Id
    private Person person;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "apptTypeId", nullable = false)
    private AppointmentType apptType;

    @Column(insertable = false, updatable = false)
    private Long sorObjectId;

    //@NotNull // TODO in tests
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorObjectId", nullable = false)
    private SORObject sorObject;

    @Size(max = 64)
    @NotNull
    @Column(nullable = false, length = 64)
    private String apptIdentifier;

    @Column
    private boolean isPrimaryAppt;

    @Column(name = "apptBeginDate")
    private Date beginDate;

    @Column(name = "apptEndDate")
    private Date endDate;

    private static final int HCB_INIT_ODDRAND = 1324735777;
    private static final int HCB_MULT_ODDRAND = 1808159071;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, apptType, sorObjectId, apptIdentifier, isPrimaryAppt,
                beginDate, endDate
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
        if (obj instanceof PersonAppointment) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((PersonAppointment) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(PersonAppointment obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((PersonAppointment) obj).getHashCodeObjects());
    }

    protected void notifyPerson() {
    }

    protected void notifyOtherPerson(Person otherPerson) {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    protected String getUid() {
        return uid;
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
                notifyOtherPerson(originalPerson);
            }
        }
    }

    public AppointmentType getApptType() {
        return apptType;
    }

    public void setApptType(AppointmentType apptType) {
        boolean changed = !Objects.equals(apptType, this.apptType);
        this.apptType = apptType;
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

    public String getApptIdentifier() {
        return apptIdentifier;
    }

    public void setApptIdentifier(String apptIdentifier) {
        boolean changed = !Objects.equals(apptIdentifier, this.apptIdentifier);
        this.apptIdentifier = apptIdentifier;
        if (changed) notifyPerson();
    }

    public boolean getIsPrimaryAppt() {
        return isPrimaryAppt;
    }

    public void setIsPrimaryAppt(boolean primaryAppt) {
        boolean changed = !Objects.equals(primaryAppt, this.isPrimaryAppt);
        isPrimaryAppt = primaryAppt;
        if (changed) notifyPerson();
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        boolean changed = !Objects.equals(beginDate, this.beginDate);
        this.beginDate = beginDate;
        if (changed) notifyPerson();
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        boolean changed = !Objects.equals(endDate, this.endDate);
        this.endDate = endDate;
        if (changed) notifyPerson();
    }
}
