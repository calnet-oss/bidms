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
import edu.berkeley.bidms.app.registryModel.model.compositeKey.SORObjectChecksumCompositeKey;
import edu.berkeley.bidms.registryModel.util.EntityUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * When {@link SORObject} data is collected, a hash code (checksum) is
 * generated for the data. This helps with various change-detection
 * facilities related to querying and processing SOR data.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@IdClass(SORObjectChecksumCompositeKey.class)
@Entity
public class SORObjectChecksum implements Serializable {

    @Id
    private int sorId;

    @Size(max = 255)
    @Id
    @Column(length = 255)
    private String sorObjKey;

    @NotNull
    @Column(nullable = false)
    private Long hash;

    @NotNull
    @Column(nullable = false)
    private Integer hashVersion;

    @NotNull
    @Column(nullable = false)
    private Date timeMarker;

    @Column
    private long numericMarker;

    private static final int HCB_INIT_ODDRAND = -986826381;
    private static final int HCB_MULT_ODDRAND = -160696885;

    private Object[] getHashCodeObjects() {
        return new Object[]{sorId, sorObjKey};
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
        if (obj instanceof SORObjectChecksum) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((SORObjectChecksum) obj).getHashCodeObjects());
        }
        return false;
    }

    public int getSorId() {
        return sorId;
    }

    public void setSorId(int sorId) {
        this.sorId = sorId;
    }

    public String getSorObjKey() {
        return sorObjKey;
    }

    public void setSorObjKey(String sorObjKey) {
        this.sorObjKey = sorObjKey;
    }

    public Long getHash() {
        return hash;
    }

    public void setHash(Long hash) {
        this.hash = hash;
    }

    public Integer getHashVersion() {
        return hashVersion;
    }

    public void setHashVersion(Integer hashVersion) {
        this.hashVersion = hashVersion;
    }

    public Date getTimeMarker() {
        return timeMarker;
    }

    public void setTimeMarker(Date timeMarker) {
        this.timeMarker = timeMarker;
    }

    public long getNumericMarker() {
        return numericMarker;
    }

    public void setNumericMarker(long numericMarker) {
        this.numericMarker = numericMarker;
    }
}
