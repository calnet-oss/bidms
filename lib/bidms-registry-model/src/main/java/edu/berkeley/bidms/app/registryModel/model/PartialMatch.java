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
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A partial match is where a new incoming SORObject may match to an existing
 * person but the certainty level isn't high enough to warrant an automatic
 * match.  Partial matches must be manually reconciled using the
 * administrative tool.  Once reconciled, the {@link PartialMatch} row is
 * removed.
 * <p>
 * Note a better term for this is "possible match" rather than "partial
 * match."  This class and table may be renamed to PossibleMatch in the
 * future.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"person", "sorObject"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"sorObjectId", "personUid"}))
@Entity
public class PartialMatch {
    protected PartialMatch() {
    }

    public PartialMatch(Person person) {
        this.person = person;
        this.personUid = person != null ? person.getUid() : null;
    }

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PartialMatch_seqgen")
    @SequenceGenerator(name = "PartialMatch_seqgen", sequenceName = "partialmatch_seq", allocationSize = 1)
    @Id
    private Long id;

    @Column(length = 64, insertable = false, updatable = false)
    private String personUid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personUid", nullable = false)
    private Person person;

    @Column(insertable = false, updatable = false)
    private Long sorObjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorObjectId", nullable = false)
    private SORObject sorObject;

    @Column(nullable = false)
    private Date dateCreated = new Date();

    @Column
    private boolean isReject = false;

    @Type(type = "edu.berkeley.bidms.registryModel.hibernate.usertype.JSONBType")
    @Column(columnDefinition = "JSONB")
    private String metaDataJson = "{}";

    @Transient
    private transient Map metaData = new LinkedHashMap();

    @PostLoad
    protected void afterLoad() throws JsonProcessingException {
        this.metaData = JsonUtil.convertJsonToMap(metaDataJson);
    }

    @PrePersist
    @PreUpdate
    protected void beforeSave() throws JsonProcessingException {
        this.metaDataJson = JsonUtil.convertMapToJson(metaData);
    }

    private static final int HCB_INIT_ODDRAND = 783238257;
    private static final int HCB_MULT_ODDRAND = -963801889;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                personUid, sorObjectId, dateCreated, isReject, metaDataJson
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
        if (obj instanceof PartialMatch) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((PartialMatch) obj).getHashCodeObjects());
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Transient
    public String getPersonUid() {
        return personUid;
    }

    public Person getPerson() {
        return person;
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

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean getIsReject() {
        return isReject;
    }

    public void setIsReject(boolean reject) {
        this.isReject = reject;
    }

    public String getMetaDataJson() {
        return metaDataJson;
    }

    public void setMetaDataJson(String metaDataJson) {
        this.metaDataJson = metaDataJson;
    }

    @Transient
    public Map getMetaData() {
        return metaData;
    }

    @Transient
    public void setMetaData(Map metaData) {
        this.metaData = metaData;
    }
}
