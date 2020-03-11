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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

/**
 * A role that can be assigned to a person.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
public class AssignableRole implements Comparable<AssignableRole> {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AssignableRole_seqgen")
    @SequenceGenerator(name = "AssignableRole_seqgen", sequenceName = "AssignableRole_seq", allocationSize = 1)
    @Id
    private Integer id;

    @Column(nullable = false, unique = true, length = 255)
    private String roleName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleCategoryId", nullable = false)
    private AssignableRoleCategory roleCategory;

    private static final int HCB_INIT_ODDRAND = 1477895935;
    private static final int HCB_MULT_ODDRAND = -1480370137;

    private Object[] getHashCodeObjects() {
        return new Object[]{roleName, roleCategory};
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
        if (obj instanceof AssignableRole) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((AssignableRole) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(AssignableRole obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((AssignableRole) obj).getHashCodeObjects());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public AssignableRoleCategory getRoleCategory() {
        return roleCategory;
    }

    public void setRoleCategory(AssignableRoleCategory roleCategory) {
        this.roleCategory = roleCategory;
    }
}
