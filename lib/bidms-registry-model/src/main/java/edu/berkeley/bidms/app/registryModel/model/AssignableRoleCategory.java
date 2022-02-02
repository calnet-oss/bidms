/*
 * Copyright (c) 2016, Regents of the University of California and
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
import com.fasterxml.jackson.annotation.JsonProperty;
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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * An {@link AssignableRole} is assigned a category that groups roles
 * together.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"parent"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"id", "roleAsgnUniquePerCat"}))
@Entity
public class AssignableRoleCategory implements Comparable<AssignableRoleCategory> {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AssignableRoleCategory_seqgen")
    @SequenceGenerator(name = "AssignableRoleCategory_seqgen", sequenceName = "AssignableRoleCategory_seq", allocationSize = 1)
    @Id
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(nullable = false, unique = true, length = 255)
    private String categoryName;

    @Column
    private boolean roleAsgnUniquePerCat;

    // only nullable if it's the root category
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parentCategoryId")
    private AssignableRoleCategory parent;

    private static final int HCB_INIT_ODDRAND = -1457329773;
    private static final int HCB_MULT_ODDRAND = 252649253;

    private Object[] getHashCodeObjects() {
        return new Object[]{categoryName, roleAsgnUniquePerCat};
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
        if (obj instanceof AssignableRoleCategory) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((AssignableRoleCategory) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(AssignableRoleCategory obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((AssignableRoleCategory) obj).getHashCodeObjects());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isRoleAsgnUniquePerCat() {
        return roleAsgnUniquePerCat;
    }

    public void setRoleAsgnUniquePerCat(boolean roleAsgnUniquePerCat) {
        this.roleAsgnUniquePerCat = roleAsgnUniquePerCat;
    }

    public AssignableRoleCategory getParent() {
        return parent;
    }

    public void setParent(AssignableRoleCategory parent) {
        this.parent = parent;
    }
}
