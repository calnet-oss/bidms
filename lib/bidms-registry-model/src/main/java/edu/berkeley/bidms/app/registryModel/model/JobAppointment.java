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
import javax.persistence.Transient;
import java.util.Date;
import java.util.Objects;

/**
 * A payroll or HR job appointment.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uid", "person", "sorObject"})
@Entity
public class JobAppointment extends PersonAppointment {
    protected JobAppointment() {
    }

    public JobAppointment(Person person) {
        super(person);
    }

    @Column(length = 64)
    private String jobCode;

    @Column(length = 255)
    private String jobTitle;

    @Column(length = 64)
    private String deptCode;

    @Column(length = 255)
    private String deptName;

    @Column
    private Date hireDate;

    // These are not persisted to the database but they are useful when
    // doing in-memory provisioning.  For example, cross referencing the
    // Peoplesoft-based SORs have effSeq and effDate as part of the job
    // primary key.
    @Transient
    private transient Integer effSeq;
    @Transient
    private transient Date effDate;

    private static final int HCB_INIT_ODDRAND = -489494045;
    private static final int HCB_MULT_ODDRAND = -620241451;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                getUid(), getApptType(), getSorObjectId(),
                getApptIdentifier(), getIsPrimaryAppt(), getBeginDate(),
                getEndDate(),
                jobCode, jobTitle, deptCode, deptName, hireDate
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
        if (obj instanceof JobAppointment) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((JobAppointment) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(PersonAppointment obj) {
        if (obj instanceof JobAppointment) {
            return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((JobAppointment) obj).getHashCodeObjects());
        } else {
            throw new IllegalArgumentException("Only objects that are an instance of this class can be compared");
        }
    }

    @Override
    protected void notifyPerson() {
        if (getPerson() != null) {
            getPerson().notifyChange(getPerson().getJobAppointments());
        }
    }

    @Override
    protected void notifyOtherPerson(Person otherPerson) {
        if (otherPerson != null) {
            otherPerson.notifyChange(otherPerson.getJobAppointments());
        }
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        boolean changed = !Objects.equals(jobCode, this.jobCode);
        this.jobCode = jobCode;
        if (changed) notifyPerson();
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        boolean changed = !Objects.equals(jobTitle, this.jobTitle);
        this.jobTitle = jobTitle;
        if (changed) notifyPerson();
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        boolean changed = !Objects.equals(deptCode, this.deptCode);
        this.deptCode = deptCode;
        if (changed) notifyPerson();
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        boolean changed = !Objects.equals(deptName, this.deptName);
        this.deptName = deptName;
        if (changed) notifyPerson();
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        boolean changed = !Objects.equals(hireDate, this.hireDate);
        this.hireDate = hireDate;
        if (changed) notifyPerson();
    }

    @Transient
    public Integer getEffSeq() {
        return effSeq;
    }

    @Transient
    public void setEffSeq(Integer effSeq) {
        this.effSeq = effSeq;
    }

    @Transient
    public Date getEffDate() {
        return effDate;
    }

    @Transient
    public void setEffDate(Date effDate) {
        this.effDate = effDate;
    }
}
