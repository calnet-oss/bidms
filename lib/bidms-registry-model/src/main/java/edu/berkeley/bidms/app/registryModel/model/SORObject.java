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
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

/**
 * Data for a person from a System of Record (SOR).  The data from the SOR is
 * stored in {@link #objJson} as JSON. SORs have their own primary key for a
 * person, stored as {@link #sorPrimaryKey}.
 * <p>
 * {@link #uid} is nullable.  In this state, the SORObject has not yet been
 * matched to a person.  This can happen if a new SORObject is not recognized
 * as active or if the SORObject is awaiting manual reconcilation as a {@link
 * PartialMatch}.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"person", "rematch"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"sorId", "sorObjKey"}))
@Entity
public class SORObject implements Comparable<SORObject> {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORObject_seqgen")
    @SequenceGenerator(name = "SORObject_seqgen", sequenceName = "SORObject_seq", allocationSize = 1)
    @Id
    private Long id;

    @Column(length = 64, insertable = false, updatable = false)
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid")
    private Person person;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sorId", nullable = false)
    private SOR sor;

    @NotNull
    @Column(name = "sorObjKey", nullable = false, length = 255)
    private String sorPrimaryKey;

    @NotNull
    @Column(name = "sorQueryTime", nullable = false)
    private Date queryTime;

    @NotNull
    @Type(type = "edu.berkeley.bidms.registryModel.hibernate.usertype.JSONBType")
    @Column(nullable = false, columnDefinition = "JSONB NOT NULL")
    private String objJson;

    @NotNull
    @Column(nullable = false)
    private Integer jsonVersion;

    @Transient
    @Column
    private Long hash;

    @Column
    private boolean rematch;

    private static final int HCB_INIT_ODDRAND = 1224078429;
    private static final int HCB_MULT_ODDRAND = 213039417;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, sor, sorPrimaryKey, jsonVersion, hash
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
        if (obj instanceof SORObject) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((SORObject) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(SORObject obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((SORObject) obj).getHashCodeObjects());
    }

    @Transient
    public Map getJson() throws JsonProcessingException {
        return JsonUtil.convertJsonToMap(objJson);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Transient
    public String getUid() {
        return uid;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    public SOR getSor() {
        return sor;
    }

    public void setSor(SOR sor) {
        this.sor = sor;
    }

    public String getSorPrimaryKey() {
        return sorPrimaryKey;
    }

    public void setSorPrimaryKey(String sorPrimaryKey) {
        this.sorPrimaryKey = sorPrimaryKey;
    }

    public Date getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(Date queryTime) {
        this.queryTime = queryTime;
    }

    public String getObjJson() {
        return objJson;
    }

    public void setObjJson(String objJson) {
        this.objJson = objJson;
    }

    public Integer getJsonVersion() {
        return jsonVersion;
    }

    public void setJsonVersion(Integer jsonVersion) {
        this.jsonVersion = jsonVersion;
    }

    @Transient
    public Long getHash() {
        return hash;
    }

    @Transient
    public void setHash(Long hash) {
        this.hash = hash;
    }

    public boolean isRematch() {
        return rematch;
    }

    public void setRematch(boolean rematch) {
        this.rematch = rematch;
    }
}
