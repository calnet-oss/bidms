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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.berkeley.bidms.registryModel.util.EntityUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
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
import java.util.Objects;

/**
 * An active role assigned to a person.  I.e., an {@link AssignableRole} assigned to
 * a person.  For an inactive role, see {@link PersonRoleArchive}.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uid", "person", "roleCategory", "roleAsgnUniquePerCat"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "roleId"}))
@Entity
public class PersonRole implements Comparable<PersonRole> {

    protected PersonRole() {
    }

    public PersonRole(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PersonRole_seqgen")
    @SequenceGenerator(name = "PersonRole_seqgen", sequenceName = "PersonRole_seq", allocationSize = 1)
    @Id
    private Long id;

    @Size(max = 64)
    @Column(length = 64, insertable = false, updatable = false)
    private String uid;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private Person person;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleCategoryId", nullable = false, foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY(roleCategoryId, roleAsgnUniquePerCat) REFERENCES AssignableRoleCategory(id, roleAsgnUniquePerCat)"))
    private AssignableRoleCategory roleCategory;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleId", nullable = false, foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY(roleId, roleCategoryId) REFERENCES AssignableRole(id, roleCategoryId)"))
    private AssignableRole role;

    @Size(max = 255)
    @Column(length = 255)
    private String roleValue;

    @Column
    private boolean roleAsgnUniquePerCat;

    private static final int HCB_INIT_ODDRAND = 1139919653;
    private static final int HCB_MULT_ODDRAND = 645001011;

    private Object[] getHashCodeObjects() {
        return new Object[]{uid, role, roleValue};
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
        if (obj instanceof PersonRole) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((PersonRole) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(PersonRole obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((PersonRole) obj).getHashCodeObjects());
    }

    private void notifyPerson() {
        if (person != null) {
            person.notifyChange(person.getAssignedRoles());
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
                originalPerson.notifyChange(originalPerson.getAssignedRoles());
            }
        }
    }

    public AssignableRole getRole() {
        return role;
    }

    public void setRole(AssignableRole role) {
        boolean changed = !Objects.equals(role, this.role);
        this.role = role;
        if (changed) notifyPerson();
    }

    public String getRoleValue() {
        return roleValue;
    }

    public void setRoleValue(String roleValue) {
        boolean changed = !Objects.equals(roleValue, this.roleValue);
        this.roleValue = roleValue;
        if (changed) notifyPerson();
    }

    public boolean isRoleAsgnUniquePerCat() {
        return roleAsgnUniquePerCat;
    }

    public void setRoleAsgnUniquePerCat(boolean roleAsgnUniquePerCat) {
        this.roleAsgnUniquePerCat = roleAsgnUniquePerCat;
    }

    public AssignableRoleCategory getRoleCategory() {
        return roleCategory;
    }

    public void setRoleCategory(AssignableRoleCategory roleCategory) {
        this.roleCategory = roleCategory;
    }
}
