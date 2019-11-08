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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import edu.berkeley.bidms.registryModel.util.EntityUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.util.Objects;

/**
 * An email address.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uid", "person", "sorObject", "emailAddressLowerCase"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "sorObjectId", "emailTypeId", "emailAddress"}))
@Entity
public class Email implements Comparable<Email> {

    protected Email() {
    }

    public Email(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Email_seqgen")
    @SequenceGenerator(name = "Email_seqgen", sequenceName = "Email_seq", allocationSize = 1)
    @Id
    private Long id;

    @Column(length = 64, insertable = false, updatable = false)
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "emailTypeId", nullable = false)
    private EmailType emailType;

    @Column(insertable = false, updatable = false)
    private Long sorObjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorObjectId", nullable = false)
    private SORObject sorObject;

    @javax.validation.constraints.Email
    @Column(length = 255)
    private String emailAddress;

    private static final int HCB_INIT_ODDRAND = -1988835543;
    private static final int HCB_MULT_ODDRAND = -1875672925;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, emailType, sorObjectId, emailAddress
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
        if (obj instanceof Email) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((Email) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(Email obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((Email) obj).getHashCodeObjects());
    }

    private void notifyPerson() {
        if (person != null) {
            person.notifyChange(person.getEmails());
        }
    }

    @Transient
    public String getEmailAddressLowerCase() {
        return emailAddress != null ? emailAddress.toLowerCase() : null;
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
                originalPerson.notifyChange(originalPerson.getEmails());
            }
        }
    }

    public EmailType getEmailType() {
        return emailType;
    }

    public void setEmailType(EmailType emailType) {
        boolean changed = !Objects.equals(emailType, this.emailType);
        this.emailType = emailType;
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

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        boolean changed = !Objects.equals(emailAddress, this.emailAddress);
        this.emailAddress = emailAddress;
        if (changed) notifyPerson();
    }
}
