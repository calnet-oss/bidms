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
package edu.berkeley.bidms.app.registryModel.model.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.berkeley.bidms.app.registryModel.model.Person;
import edu.berkeley.bidms.app.registryModel.model.type.MatchHistoryResultTypeEnum;
import edu.berkeley.bidms.common.json.JsonUtil;
import edu.berkeley.bidms.orm.hibernate.usertype.JSONBType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Type;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
public class MatchHistory {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MatchHistory_seqgen")
    @SequenceGenerator(name = "MatchHistory_seqgen", sequenceName = "MatchHistory_seq", allocationSize = 1)
    @Id
    private Long id;

    @NotNull
    @Size(max = 36)
    @Column(length = 36, nullable = false, columnDefinition = "CHAR(36)")
    private String eventId;

    @NotNull
    @Column(nullable = false)
    private Integer sorId;

    @NotNull
    @Column(nullable = false)
    private Long sorObjectId;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false, name = "sorObjKey", length = 255)
    private String sorPrimaryKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 63, nullable = false)
    private MatchHistoryResultTypeEnum matchResultType;

    /**
     * The time when the match result was produced.
     */
    @NotNull
    @Column(nullable = false)
    private Date actionTime;

    @Size(max = 64)
    @Column(length = 64)
    private String uidAssigned;

    @Size(max = 64)
    @Column(length = 64)
    private String doneByUid;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doneByUid", insertable = false, updatable = false)
    private Person doneBy;

    @JsonIgnore
    @NotNull
    @Type(JSONBType.class)
    @Column(columnDefinition = "JSONB NOT NULL", nullable = false)
    private String metaDataJson = "{}";

    @Transient
    private transient MatchHistoryMetaData metaData = new MatchHistoryMetaData();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String matchEventId) {
        this.eventId = matchEventId;
    }

    public Integer getSorId() {
        return sorId;
    }

    public void setSorId(Integer sorId) {
        this.sorId = sorId;
    }

    public Long getSorObjectId() {
        return sorObjectId;
    }

    public void setSorObjectId(Long sorObjectId) {
        this.sorObjectId = sorObjectId;
    }

    public String getSorPrimaryKey() {
        return sorPrimaryKey;
    }

    public void setSorPrimaryKey(String sorObjKey) {
        this.sorPrimaryKey = sorObjKey;
    }

    public MatchHistoryResultTypeEnum getMatchResultType() {
        return matchResultType;
    }

    public void setMatchResultType(MatchHistoryResultTypeEnum matchResultType) {
        this.matchResultType = matchResultType;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }

    public String getUidAssigned() {
        return uidAssigned;
    }

    public void setUidAssigned(String newUidAssigned) {
        this.uidAssigned = newUidAssigned;
    }

    public String getDoneByUid() {
        return doneByUid;
    }

    public void setDoneByUid(String doneByUid) {
        this.doneByUid = doneByUid;
    }

    public Person getDoneBy() {
        return doneBy;
    }

    @PostLoad
    public void afterLoad() throws JsonProcessingException {
        this.metaData = JsonUtil.convertJsonToObject(metaDataJson, MatchHistoryMetaData.class);
    }

    @PrePersist
    @PreUpdate
    public void beforeSave() throws JsonProcessingException {
        this.metaDataJson = JsonUtil.convertObjectToJson(metaData);
    }

    @Transient
    public MatchHistoryMetaData getMetaData() {
        return metaData;
    }

    @Transient
    public void setMetaData(MatchHistoryMetaData metaData) {
        this.metaData = metaData;
    }

    public String getMetaDataJson() {
        return metaDataJson;
    }
}
