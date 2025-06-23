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
package edu.berkeley.bidms.app.registryModel.model.auth;

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
 * A {@link RegistryRole} is a role that can be assigned to a {@link
 * RegistryUser} service account.  REST endpoints can grant access to
 * authenticated users with the appropriate role using Spring Security.  (In
 * Spring Security lingo, these roles are the "authorities".)
 */
@Entity
public class RegistryRole implements Comparable<RegistryRole>, Serializable {

    protected RegistryRole() {
    }

    public RegistryRole(String authority) {
        this.authority = authority;
    }

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RegistryRole_seqgen")
    @SequenceGenerator(name = "RegistryRole_seqgen", sequenceName = "RegistryRole_seq", allocationSize = 1)
    @Id
    private Integer id;

    @Size(max = 127)
    @NotNull
    @Column(nullable = false, unique = true, length = 127)
    private String authority;

    @Override
    public String toString() {
        return authority;
    }

    private static final int HCB_INIT_ODDRAND = 673234685;
    private static final int HCB_MULT_ODDRAND = -1601038317;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                authority
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
        if (obj instanceof RegistryRole) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((RegistryRole) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(RegistryRole obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((RegistryRole) obj).getHashCodeObjects());
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
