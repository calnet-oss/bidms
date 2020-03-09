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
import java.util.Map;
import java.util.Objects;

/**
 * A DownstreamObject is data written to a system downstream from the
 * identity registry. Examples of this could be DownstreamObjects for LDAP or
 * Active Directory. The data to be written to these downstream systems is
 * contained as JSON in the {@link #objJson} property.
 */
// so that our rendered map includes nulls, which is important for the
// downstream provisioning engine to know which attributes to clear out
// downstream
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties({"uid", "person", "objJson"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"systemId", "sysObjKey"}))
@Entity
public class DownstreamObject implements Comparable<DownstreamObject> {

    protected DownstreamObject() {
    }

    public DownstreamObject(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DownstreamObject_seqgen")
    @SequenceGenerator(name = "DownstreamObject_seqgen", sequenceName = "DownstreamObject_seq", allocationSize = 1)
    @Id
    private Long id;

    @Column(length = 64, insertable = false, updatable = false)
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "systemId", nullable = false)
    private DownstreamSystem system;

    @Column(name = "sysObjKey", nullable = false, length = 255)
    private String systemPrimaryKey;

    @Type(type = "edu.berkeley.bidms.registryModel.hibernate.usertype.JSONBType")
    @Column(nullable = false, columnDefinition = "JSONB NOT NULL")
    private String objJson;

    // always updated by DB trigger
    @Transient
    @Column(nullable = false, insertable = false, updatable = false)
    private Long hash;

    @Column(nullable = false)
    private Integer ownershipLevel;

    @Column(length = 64)
    private String globUniqId;

    @Column
    private boolean forceProvision;

    @Transient
    public Map getJson() throws JsonProcessingException {
        // convert to a map and include nulls
        return JsonUtil.convertJsonToMap(objJson);
    }

    private static final int HCB_INIT_ODDRAND = -179518623;
    private static final int HCB_MULT_ODDRAND = 632978157;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, system, systemPrimaryKey, objJson, ownershipLevel,
                globUniqId, forceProvision
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
        if (obj instanceof DownstreamObject) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((DownstreamObject) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(DownstreamObject obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((DownstreamObject) obj).getHashCodeObjects());
    }

    private void notifyPerson() {
        if (person != null) {
            person.notifyChange(person.getDownstreamObjects());
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
                originalPerson.notifyChange(originalPerson.getDownstreamObjects());
            }
        }
    }

    public DownstreamSystem getSystem() {
        return system;
    }

    public void setSystem(DownstreamSystem system) {
        boolean changed = !Objects.equals(system, this.system);
        this.system = system;
        if (changed) notifyPerson();
    }

    public String getSystemPrimaryKey() {
        return systemPrimaryKey;
    }

    public void setSystemPrimaryKey(String systemPrimaryKey) {
        boolean changed = !Objects.equals(systemPrimaryKey, this.systemPrimaryKey);
        this.systemPrimaryKey = systemPrimaryKey;
        if (changed) notifyPerson();
    }

    public String getObjJson() {
        return objJson;
    }

    public void setObjJson(String objJson) {
        boolean changed = !Objects.equals(objJson, this.objJson);
        this.objJson = objJson;
        if (changed) notifyPerson();
    }

    @Transient
    public Long getHash() {
        return hash;
    }

    @Transient
    public void setHash(Long hash) {
        boolean changed = !Objects.equals(hash, this.hash);
        this.hash = hash;
        if (changed) notifyPerson();
    }

    public Integer getOwnershipLevel() {
        return ownershipLevel;
    }

    public void setOwnershipLevel(Integer ownershipLevel) {
        boolean changed = !Objects.equals(ownershipLevel, this.ownershipLevel);
        this.ownershipLevel = ownershipLevel;
        if (changed) notifyPerson();
    }

    public String getGlobUniqId() {
        return globUniqId;
    }

    public void setGlobUniqId(String globUniqId) {
        boolean changed = !Objects.equals(globUniqId, this.globUniqId);
        this.globUniqId = globUniqId;
        if (changed) notifyPerson();
    }

    public boolean isForceProvision() {
        return forceProvision;
    }

    public void setForceProvision(boolean forceProvision) {
        boolean changed = !Objects.equals(forceProvision, this.forceProvision);
        this.forceProvision = forceProvision;
        if (changed) notifyPerson();
    }
}
