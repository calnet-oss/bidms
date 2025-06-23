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

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.berkeley.bidms.registryModel.util.EntityUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;

import java.util.Date;

/**
 * This is used by the registry provisioning phase to detect when the
 * aggregate SORObject data has changed since the last time it was
 * processed.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
public class PersonSorObjectsSyncKey {
    @Size(max = 64)
    @Id
    @Column(name = "uid", nullable = false, length = 64)
    private String id; // uid

    @Size(max = 32)
    @Column(length = 32)
    private String provisionedJsonHash;

    @Column
    private boolean forceProvision;

    @Column
    private Date timeUpdated; // normally updated by a trigger, but sometimes we want to force persistence by modifying this column

    private static final int HCB_INIT_ODDRAND = -1734048115;
    private static final int HCB_MULT_ODDRAND = 908600341;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                id, provisionedJsonHash, forceProvision
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
        if (obj instanceof PersonSorObjectsSyncKey) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((PersonSorObjectsSyncKey) obj).getHashCodeObjects());
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProvisionedJsonHash() {
        return provisionedJsonHash;
    }

    public void setProvisionedJsonHash(String provisionedJsonHash) {
        this.provisionedJsonHash = provisionedJsonHash;
    }

    public boolean isForceProvision() {
        return forceProvision;
    }

    public void setForceProvision(boolean forceProvision) {
        this.forceProvision = forceProvision;
    }

    public Date getTimeUpdated() {
        return timeUpdated;
    }

    public void setTimeUpdated(Date timeUpdated) {
        this.timeUpdated = timeUpdated;
    }
}
