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

import edu.berkeley.bidms.orm.hibernate.usertype.JSONBType;
import edu.berkeley.bidms.registryModel.util.EntityUtil;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;

/**
 * This is an externally materialized agglomeration of all the JSON from all
 * the SORObjects for a person into one JSON structure. This is useful during
 * the identity provisioning phase where SOR data is transformed to registry
 * data.
 * <p>
 * Since this is externally materialized, this is a read-only entity.
 */
@Entity
public class PersonSorObjectsJson {
    @Size(max = 64)
    @Id
    @Column(name = "uid", nullable = false, length = 64)
    private String id; // uid

    @NotNull
    @Column(nullable = false)
    private Date lastUpdated;

    @NotNull
    @Type(JSONBType.class)
    @Column(nullable = false, columnDefinition = "JSONB NOT NULL")
    private String aggregateJson;

    @Size(max = 32)
    @NotNull
    @Column(nullable = false, length = 32)
    private String jsonHash;

    @Size(max = 32)
    @Column(length = 32)
    private String provisionedJsonHash;

    @Column
    private Date lastProvisioned;

    @Column
    private boolean forceProvision;

    /**
     * PersonSorObjectsJson is a read-only table, but tests may override this
     * method to do nothing so that mock data can be written.  The default
     * implementation throws a RuntimeException.
     */
    protected void enforceReadOnly() {
        throw new RuntimeException("PersonSorObjectsJson is read-only");
    }

    @PrePersist
    @PreUpdate
    @PreRemove
    protected void beforeSaveOrDelete() {
        enforceReadOnly();
    }

    private static final int HCB_INIT_ODDRAND = 2105038243;
    private static final int HCB_MULT_ODDRAND = 93021719;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                id, lastUpdated, aggregateJson, jsonHash,
                provisionedJsonHash, lastProvisioned, forceProvision
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
        if (obj instanceof PersonSorObjectsJson) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((PersonSorObjectsJson) obj).getHashCodeObjects());
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getAggregateJson() {
        return aggregateJson;
    }

    public void setAggregateJson(String aggregateJson) {
        this.aggregateJson = aggregateJson;
    }

    public String getJsonHash() {
        return jsonHash;
    }

    public void setJsonHash(String jsonHash) {
        this.jsonHash = jsonHash;
    }

    public String getProvisionedJsonHash() {
        return provisionedJsonHash;
    }

    public void setProvisionedJsonHash(String provisionedJsonHash) {
        this.provisionedJsonHash = provisionedJsonHash;
    }

    public Date getLastProvisioned() {
        return lastProvisioned;
    }

    public void setLastProvisioned(Date lastProvisioned) {
        this.lastProvisioned = lastProvisioned;
    }

    public boolean isForceProvision() {
        return forceProvision;
    }

    public void setForceProvision(boolean forceProvision) {
        this.forceProvision = forceProvision;
    }
}
