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

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.berkeley.bidms.registryModel.util.EntityUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * A type for {@link Telephone}.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
public class TelephoneType implements Comparable<TelephoneType> {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TelephoneType_seqgen")
    @SequenceGenerator(name = "TelephoneType_seqgen", sequenceName = "TelephoneType_seq", allocationSize = 1)
    @Id
    private Integer id;

    @Column(nullable = false, unique = true, length = 64)
    private String telephoneTypeName;

    private static final int HCB_INIT_ODDRAND = 617858821;
    private static final int HCB_MULT_ODDRAND = -2122716615;

    private Object[] getHashCodeObjects() {
        return new Object[]{telephoneTypeName};
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
        if (obj instanceof TelephoneType) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((TelephoneType) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(TelephoneType obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((TelephoneType) obj).getHashCodeObjects());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTelephoneTypeName() {
        return telephoneTypeName;
    }

    public void setTelephoneTypeName(String telephoneTypeName) {
        this.telephoneTypeName = telephoneTypeName;
    }
}
