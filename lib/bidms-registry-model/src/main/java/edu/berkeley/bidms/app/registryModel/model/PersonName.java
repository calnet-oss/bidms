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
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.berkeley.bidms.registryModel.util.EntityUtil;
import edu.berkeley.bidms.common.json.JsonUtil;
import org.hibernate.annotations.Type;

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
import java.util.List;
import java.util.Objects;

/**
 * A person's name.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uid", "person", "sorObject", "honorifics"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "sorObjectId", "nameTypeId"}))
@Entity
public class PersonName implements Comparable<PersonName> {

    protected PersonName() {
    }

    public PersonName(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PersonName_seqgen")
    @SequenceGenerator(name = "PersonName_seqgen", sequenceName = "PersonName_seq", allocationSize = 1)
    @Id
    private Long id;

    @Column(length = 64, insertable = false, updatable = false)
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nameTypeId", nullable = false)
    private NameType nameType;

    @Column(insertable = false, updatable = false)
    private Long sorObjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorObjectId", nullable = false)
    private SORObject sorObject;

    @Column(length = 32)
    private String prefix;

    @Column(length = 127)
    private String givenName;

    @Column(length = 127)
    private String middleName;

    @Column(length = 127)
    private String surName;

    @Column(length = 32)
    private String suffix;

    @Column(length = 1023)
    private String fullName;

    @Type(type = "edu.berkeley.bidms.registryModel.hibernate.usertype.JSONBType")
    @Column(columnDefinition = "JSONB")
    private String honorifics; // This is a JSON array of strings.  Stored in PostGreSQL as JSONB.

    @Column
    private boolean isPrimary;

    @Transient
    public List getHonorificsAsList() throws JsonProcessingException {
        return honorifics != null ? JsonUtil.convertJsonToList(honorifics) : null;
    }

    @Transient
    public void setHonorificsAsList(List honorificsAsList) throws JsonProcessingException {
        this.honorifics = (honorificsAsList != null ? JsonUtil.convertListToJson(honorificsAsList) : null);
    }

    private static final int HCB_INIT_ODDRAND = -525837835;
    private static final int HCB_MULT_ODDRAND = 958447793;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, nameType, sorObjectId, prefix, givenName, middleName,
                surName, suffix, fullName, honorifics, isPrimary
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
        if (obj instanceof PersonName) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((PersonName) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(PersonName obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((PersonName) obj).getHashCodeObjects());
    }

    private void notifyPerson() {
        if (person != null) {
            person.notifyChange(person.getNames());
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
                originalPerson.notifyChange(originalPerson.getNames());
            }
        }
    }

    public NameType getNameType() {
        return nameType;
    }

    public void setNameType(NameType nameType) {
        boolean changed = !Objects.equals(nameType, this.nameType);
        this.nameType = nameType;
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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        boolean changed = !Objects.equals(prefix, this.prefix);
        this.prefix = prefix;
        if (changed) notifyPerson();
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        boolean changed = !Objects.equals(givenName, this.givenName);
        this.givenName = givenName;
        if (changed) notifyPerson();
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        boolean changed = !Objects.equals(middleName, this.middleName);
        this.middleName = middleName;
        if (changed) notifyPerson();
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        boolean changed = !Objects.equals(surName, this.surName);
        this.surName = surName;
        if (changed) notifyPerson();
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        boolean changed = !Objects.equals(suffix, this.suffix);
        this.suffix = suffix;
        if (changed) notifyPerson();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        boolean changed = !Objects.equals(fullName, this.fullName);
        this.fullName = fullName;
        if (changed) notifyPerson();
    }

    public String getHonorifics() {
        return honorifics;
    }

    public void setHonorifics(String honorifics) {
        boolean changed = !Objects.equals(honorifics, this.honorifics);
        this.honorifics = honorifics;
        if (changed) notifyPerson();
    }

    public boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(boolean primary) {
        boolean changed = !Objects.equals(primary, this.isPrimary);
        isPrimary = primary;
        if (changed) notifyPerson();
    }
}
