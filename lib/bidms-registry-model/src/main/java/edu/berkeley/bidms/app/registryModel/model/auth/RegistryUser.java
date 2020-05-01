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

import edu.berkeley.bidms.orm.collection.RebuildableSortedSet;
import edu.berkeley.bidms.orm.collection.RebuildableTreeSet;
import edu.berkeley.bidms.registryModel.util.EntityUtil;
import edu.berkeley.bidms.springsecurity.api.user.CredentialsAwareUser;
import org.hibernate.annotations.CollectionType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import java.io.Serializable;

/**
 * A {@link RegistryUser} is a service account stored internally in the
 * registry used for authentication to registry services (such as REST
 * endpoints).
 */
@Entity
public class RegistryUser implements Serializable, Comparable<RegistryUser>, CredentialsAwareUser {

    protected RegistryUser() {
    }

    /**
     * Use {@link edu.berkeley.bidms.springsecurity.api.service.CredentialSetter#setPassword(CredentialsAwareUser,
     * String)} to set the password for this user.
     */
    public RegistryUser(String username) {
        this.username = username;
    }

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RegistryUser_seqgen")
    @SequenceGenerator(name = "RegistryUser_seqgen", sequenceName = "RegistryUser_seq", allocationSize = 1)
    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 127)
    private String username;

    // Strong hash for use when Digest Auth is not in effect
    // (spring-security useDigestAuth=false).
    @Column(nullable = false, length = 128)
    private String passwordHash;

    // MD5 hash of "username:realm:password" for services that need HTTP
    // Digest Auth (spring-security useDigestAuth=true).
    @Column(nullable = false, length = 32)
    private String passwordHttpDigestHash;

    @Column
    private boolean enabled = true;
    @Column
    private boolean accountExpired;
    @Column
    private boolean accountLocked;
    @Column
    private boolean passwordExpired;

    // TODO: deprecated salt column needs to be removed from RegistryUser table

    @SuppressWarnings("JpaAttributeTypeInspection")
    @ManyToMany
    @JoinTable(
            name = "RegistryUserRole",
            joinColumns = @JoinColumn(name = "registryUserId"),
            inverseJoinColumns = @JoinColumn(name = "registryRoleId")
    )
    @OrderBy("authority")
    @CollectionType(type = "edu.berkeley.bidms.registryModel.hibernate.usertype.auth.RegistryRoleCollectionType")
    private RebuildableSortedSet<RegistryRole> roles = new RebuildableTreeSet<>();

    @Override
    public String toString() {
        return username;
    }

    public boolean isActive() {
        return isEnabled() && !isAccountExpired() && !isAccountLocked();
    }

    private static final int HCB_INIT_ODDRAND = 1247170383;
    private static final int HCB_MULT_ODDRAND = 488236461;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                username, passwordHash, passwordHttpDigestHash, enabled,
                accountExpired, accountLocked, passwordExpired, roles
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
        if (obj instanceof RegistryUser) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((RegistryUser) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(RegistryUser obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((RegistryUser) obj).getHashCodeObjects());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordHttpDigestHash() {
        return passwordHttpDigestHash;
    }

    public void setPasswordHttpDigestHash(String passwordHttpDigestHash) {
        this.passwordHttpDigestHash = passwordHttpDigestHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAccountExpired() {
        return accountExpired;
    }

    public void setAccountExpired(boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }

    public RebuildableSortedSet<RegistryRole> getRoles() {
        return roles;
    }

    public void setRoles(RebuildableSortedSet<RegistryRole> roles) {
        this.roles = roles;
    }

    public void addToRoles(RegistryRole role) {
        roles.add(role);
    }

    public void removeFromRoles(RegistryRole role) {
        roles.remove(role);
    }
}
