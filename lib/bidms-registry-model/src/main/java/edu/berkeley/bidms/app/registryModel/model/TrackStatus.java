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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.berkeley.bidms.common.json.JsonUtil;
import edu.berkeley.bidms.registryModel.util.EntityUtil;
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
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A {@link TrackStatus} entry is a simple way of tracking various status
 * flags for a person.  The person either has the flag or doesn't, although
 * meta data can be associated with the flag.  These status flags are
 * strings, stored in {@link #trackStatusType} and the meta data stored as
 * JSON in {@link #metaDataJson}.
 * <p>
 * The meta data feature is not intended for complex data storage.  If you
 * find meta data being overused, the data likely belongs in a new table
 * instead (or as its own SORObject).
 * <p>
 * {@link TrackStatus} is also not appropriate for information more suitable
 * as a role.  For that, see {@link AssignableRole} and {@link PersonRole}.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "trackStatusType"}))
@Entity
public class TrackStatus implements Comparable<TrackStatus> {

    protected TrackStatus() {
    }

    public TrackStatus(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TrackStatus_seqgen")
    @SequenceGenerator(name = "TrackStatus_seqgen", sequenceName = "TrackStatus_seq", allocationSize = 1)
    @Id
    private Long id;

    @JsonIgnore
    @Column(length = 64, insertable = false, updatable = false)
    private String uid;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    @NotNull
    private Person person;

    @Column(nullable = false, length = 64)
    @NotNull
    private String trackStatusType;

    @Column(insertable = false, updatable = false)
    private Date timeCreated;

    @Column(length = 256)
    private String description;

    @JsonIgnore
    @Type(type = "edu.berkeley.bidms.orm.hibernate.usertype.JSONBType")
    @Column(columnDefinition = "JSONB")
    private String metaDataJson = "{}";

    @Transient
    private transient Map metaData = new LinkedHashMap();

    private static final int HCB_INIT_ODDRAND = 1747827797;
    private static final int HCB_MULT_ODDRAND = 1399519367;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, trackStatusType, timeCreated, description, metaDataJson
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
        if (obj instanceof TrackStatus) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((TrackStatus) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(TrackStatus obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((TrackStatus) obj).getHashCodeObjects());
    }

    private void notifyPerson() {
        if (person != null) {
            person.notifyChange(person.getTrackStatuses());
        }
    }

    @PostLoad
    public void afterLoad() throws JsonProcessingException {
        this.metaData = JsonUtil.convertJsonToMap(metaDataJson);
    }

    @PrePersist
    @PreUpdate
    public void beforeSave() throws JsonProcessingException {
        this.metaDataJson = JsonUtil.convertMapToJson(metaData);
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
                originalPerson.notifyChange(originalPerson.getTrackStatuses());
            }
        }
    }

    public String getTrackStatusType() {
        return trackStatusType;
    }

    public void setTrackStatusType(String trackStatusType) {
        boolean changed = !Objects.equals(trackStatusType, this.trackStatusType);
        this.trackStatusType = trackStatusType;
        if (changed) notifyPerson();
    }

    public Date getTimeCreated() {
        return timeCreated;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        boolean changed = !Objects.equals(description, this.description);
        this.description = description;
        if (changed) notifyPerson();
    }

    public String getMetaDataJson() {
        return metaDataJson;
    }

    public void setMetaDataJson(String metaDataJson) {
        boolean changed = !Objects.equals(metaDataJson, this.metaDataJson);
        this.metaDataJson = metaDataJson;
        if (changed) notifyPerson();
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
