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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import edu.berkeley.bidms.common.json.JsonUtil;
import edu.berkeley.bidms.orm.hibernate.usertype.JSONBType;
import edu.berkeley.bidms.registryModel.util.EntityUtil;
import org.hibernate.annotations.Type;

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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
@JsonIgnoreProperties({"uid", "rematch", "sor"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"sorId", "sorObjKey"}))
@Entity
public class SORObject implements Comparable<SORObject> {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORObject_seqgen")
    @SequenceGenerator(name = "SORObject_seqgen", sequenceName = "SORObject_seq", allocationSize = 1)
    @Id
    private Long id;

    @Size(max = 64)
    @Column(length = 64, insertable = false, updatable = false)
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid")
    private Person person;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sorId", nullable = false)
    private SOR sor;

    @Size(max = 255)
    @NotNull
    @Column(name = "sorObjKey", nullable = false, length = 255)
    private String sorPrimaryKey;

    @NotNull
    @Column(name = "sorQueryTime", nullable = false)
    private Date queryTime;

    @JsonIgnore
    @NotNull
    @Type(JSONBType.class)
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

    @JsonProperty(value = "objJson", access = JsonProperty.Access.READ_ONLY)
    public Map getJson() throws JsonProcessingException {
        return JsonUtil.convertJsonToMap(objJson);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    @JsonSerialize(using = PersonForSorObjectSerializer.class)
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

    @JsonProperty(value = "sorName", access = JsonProperty.Access.READ_ONLY)
    public String getSorName() {
        return sor.getName();
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

    protected static class PersonForSorObjectSerializer extends StdSerializer<Person> {
        protected PersonForSorObjectSerializer() {
            this(null);
        }

        protected PersonForSorObjectSerializer(Class<Person> t) {
            super(t);
        }

        @Override
        public void serialize(Person p, JsonGenerator gen, SerializerProvider provider) throws IOException {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            if (p != null) {
                gen.writeStartObject();
                gen.writeStringField("uid", p.getUid());
                if (p.getTimeCreated() != null) {
                    gen.writeStringField("timeCreated", sdf.format(p.getTimeCreated()));
                }
                if (p.getTimeUpdated() != null) {
                    gen.writeStringField("timeUpdated", sdf.format(p.getTimeUpdated()));
                }
                if (p.getNames() != null && p.getNames().size() > 0) {
                    List<String> names = p.getNames().stream().map(PersonName::getFullName).distinct().sorted().collect(Collectors.toList());
                    String[] namesArray = new String[names.size()];
                    names.toArray(namesArray);
                    gen.writeArrayFieldStart("names");
                    for (String fullName : namesArray) {
                        gen.writeString(fullName);
                    }
                    gen.writeEndArray();
                }
                gen.writeEndObject();
            }
        }
    }
}