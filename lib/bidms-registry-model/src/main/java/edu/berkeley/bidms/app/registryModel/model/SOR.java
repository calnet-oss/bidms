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
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.berkeley.bidms.registryModel.util.EntityUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * A System of Record (SOR).  A system of record is an upstream system that
 * contains identity data for a person (which is stored in {@link
 * SORObject}s).  SOR data is received, aggregated and transformed to
 * identity registry data.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
public class SOR implements Serializable, Comparable<SOR> {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Sor_seqgen")
    @SequenceGenerator(name = "Sor_seqgen", sequenceName = "sor_seq", allocationSize = 1)
    @Column(name = "sorId")
    @Id
    private Integer id;

    @Size(max = 64)
    @NotNull
    @Column(name = "sorName", nullable = false, unique = true, length = 64)
    private String name;

    private static final int HCB_INIT_ODDRAND = 338981711;
    private static final int HCB_MULT_ODDRAND = -1955368013;

    private Object[] getHashCodeObjects() {
        return new Object[]{name};
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
        if (obj instanceof SOR) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((SOR) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(SOR obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((SOR) obj).getHashCodeObjects());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
