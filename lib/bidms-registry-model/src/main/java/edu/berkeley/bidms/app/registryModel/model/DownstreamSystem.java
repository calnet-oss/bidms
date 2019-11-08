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
 * A downstream system to assign to a {@link DownstreamObject}.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
public class DownstreamSystem implements Comparable<DownstreamSystem> {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DownstreamSystem_seqgen")
    @SequenceGenerator(name = "DownstreamSystem_seqgen", sequenceName = "DownstreamSystem_seq", allocationSize = 1)
    @Id
    private Integer id;

    @Column(name = "systemName", nullable = false, unique = true, length = 64)
    private String name;

    private static final int HCB_INIT_ODDRAND = -1488966537;
    private static final int HCB_MULT_ODDRAND = 559844279;

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
        if (obj instanceof DownstreamSystem) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((DownstreamSystem) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(DownstreamSystem obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((DownstreamSystem) obj).getHashCodeObjects());
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
