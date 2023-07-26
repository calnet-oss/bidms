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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * A security token from a SOR.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uid", "person", "sorObject", "token"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "sorTokenTypeId", "token"}))
@Entity
public class SORToken implements Comparable<SORToken> {
    protected SORToken() {
    }

    public SORToken(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORToken_seqgen")
    @SequenceGenerator(name = "SORToken_seqgen", sequenceName = "SORToken_seq", allocationSize = 1)
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
    @JoinColumn(name = "sorTokenTypeId", nullable = false)
    private SORTokenType tokenType;

    @Column(insertable = false, updatable = false)
    private Long sorObjectId;

    //@NotNull // TODO in tests
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorObjectId", nullable = false)
    private SORObject sorObject;

    @NotNull
    @Size(max = 32)
    @Column(length = 32, nullable = false)
    private String token;

    private Date expirationTime;

    private static final int HCB_INIT_ODDRAND = -593449167;
    private static final int HCB_MULT_ODDRAND = -804716159;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, tokenType, sorObjectId, token, expirationTime
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
        if (obj instanceof SORToken) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((SORToken) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(SORToken obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, obj.getHashCodeObjects());
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
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    public SORTokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(SORTokenType tokenType) {
        this.tokenType = tokenType;
    }

    @Transient
    public Long getSorObjectId() {
        return sorObjectId;
    }

    public SORObject getSorObject() {
        return sorObject;
    }

    public void setSorObject(SORObject sorObject) {
        this.sorObject = sorObject;
        this.sorObjectId = sorObject != null ? sorObject.getId() : null;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }
}
