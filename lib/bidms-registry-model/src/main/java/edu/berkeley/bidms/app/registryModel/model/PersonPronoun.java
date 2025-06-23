/*
 * Copyright (c) 2023, Regents of the University of California and
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

import java.util.Objects;

/**
 * A person's pronoun.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uid", "person", "sorObject"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "sorObjectId", "pronounTypeId"}))
@Entity
public class PersonPronoun implements Comparable<PersonPronoun> {

    protected PersonPronoun() {
    }

    public PersonPronoun(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PersonPronoun_seqgen")
    @SequenceGenerator(name = "PersonPronoun_seqgen", sequenceName = "PersonPronoun_seq", allocationSize = 1)
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
    @JoinColumn(name = "pronounTypeId", nullable = false)
    private PronounType pronounType;

    @Column(insertable = false, updatable = false)
    private Long sorObjectId;

    //@NotNull // TODO in tests
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorObjectId", nullable = false)
    private SORObject sorObject;

    @Size(max = 64)
    @Column(length = 64)
    private String pronoun;

    @Size(max = 64)
    @Column(length = 64)
    private String pronounCode;

    @Column
    private boolean isPrimary;

    private static final int HCB_INIT_ODDRAND = 1467196133;
    private static final int HCB_MULT_ODDRAND = -2002407947;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, pronounType, sorObjectId, pronoun, pronounCode, isPrimary
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
        if (obj instanceof PersonPronoun) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((PersonPronoun) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(PersonPronoun obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((PersonPronoun) obj).getHashCodeObjects());
    }

    private void notifyPerson() {
        if (person != null) {
            person.notifyChange(person.getPronouns());
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
                originalPerson.notifyChange(originalPerson.getPronouns());
            }
        }
    }

    public PronounType getPronounType() {
        return pronounType;
    }

    public void setPronounType(PronounType pronounType) {
        boolean changed = !Objects.equals(pronounType, this.pronounType);
        this.pronounType = pronounType;
        if (changed) notifyPerson();
    }

    public String getPronoun() {
        return pronoun;
    }

    public void setPronoun(String pronoun) {
        boolean changed = !Objects.equals(pronoun, this.pronoun);
        this.pronoun = pronoun;
        if (changed) notifyPerson();
    }

    public String getPronounCode() {
        return pronounCode;
    }

    public void setPronounCode(String pronounCode) {
        boolean changed = !Objects.equals(pronounCode, this.pronounCode);
        this.pronounCode = pronounCode;
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

    public boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(boolean primary) {
        boolean changed = !Objects.equals(primary, this.isPrimary);
        isPrimary = primary;
        if (changed) notifyPerson();
    }
}
