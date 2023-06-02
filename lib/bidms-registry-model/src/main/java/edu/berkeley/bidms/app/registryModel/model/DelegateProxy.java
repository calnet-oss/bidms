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
import edu.berkeley.bidms.app.registryModel.repo.IdentifierRepository;
import edu.berkeley.bidms.app.registryModel.repo.IdentifierTypeRepository;
import edu.berkeley.bidms.registryModel.util.EntityUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

/**
 * A delegator can nominate a delegatee (aka the delegate proxy) to have
 * certain access rights to the delegator's information or to perform certain
 * tasks on behalf of the delegator. An example: A parent may be assigned as
 * a delegate proxy for a student.
 * <p>
 * The "delegate proxy" (e.g. parent) has their own uid.  This is the uid
 * assigned in this delegate proxy object. The delegator (e.g. student) is
 * identified by a system of record primary key in the {@link #proxyForId}
 * property.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uid", "person", "delegateProxySorObject", "proxyForIdentifier", "proxyForPerson"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "sourceProxyId", "delegateProxyTypeId", "proxyForId"}))
@Entity
public class DelegateProxy implements Comparable<DelegateProxy> {

    protected DelegateProxy() {
    }

    public DelegateProxy(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DelegateProxy_seqgen")
    @SequenceGenerator(name = "DelegateProxy_seqgen", sequenceName = "DelegateProxy_seq", allocationSize = 1)
    @Id
    private Long id; // this id is internal, generated by the sequence

    @Size(max = 64)
    @Column(length = 64, insertable = false, updatable = false)
    private String uid;

    // the Person object (and thus uid) of the proxy delegate
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private Person person;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "delegateProxyTypeId", nullable = false)
    private DelegateProxyType delegateProxyType;

    // information about the proxy delegate
    @Size(max = 64)
    @NotNull
    @Column(nullable = false, length = 64)
    private String sourceProxyId; // primary key of the delegate proxy in the source system (ex: the CS_DELEGATE SCC_DA_PRXY_ID)
    @Column(insertable = false, updatable = false)
    private Long delegateProxySorObjectId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegateProxySorObjectId")
    private SORObject delegateProxySorObject; // The optional SORObject the delegate data is sourced from
    @Size(max = 64)
    @Column(length = 64)
    private String delegateProxySecurityKey; // The security key generated for the proxy to claim an account (ex: the CS_DELEGATE SCC_DA_SECURTY_KEY)

    // information about the person the delegate is a proxy for
    @Size(max = 64)
    @NotNull
    @Column(nullable = false, length = 64)
    private String proxyForId; // the source system identifier this delegate is a proxy for (ex: the CS_DELEGATE EMPLID)

    /**
     * Lazy loads the proxyForIdentifier
     */
    @Transient
    public Identifier getProxyForIdentifier(IdentifierTypeRepository identifierTypeRepository, IdentifierRepository identifierRepository, String identifierTypeName) {
        // identifierTypeName can be retrieved using DelegateProxyTypeEnum.getEnum(delegateProxyType).getIdentifierTypeEnum().getName()
        IdentifierType identifierType = identifierTypeRepository.findByIdName(identifierTypeName);
        List<Identifier> all = identifierRepository.findAllByIdentifierTypeAndIdentifier(identifierType, proxyForId);
        return all.size() > 0 ? all.iterator().next() : null;
    }

    /**
     * Lazy loads the proxyForPerson
     */
    @Transient
    public Person getProxyForPerson(IdentifierTypeRepository identifierTypeRepository, IdentifierRepository identifierRepository, String identifierTypeName) {
        Identifier identifier = getProxyForIdentifier(identifierTypeRepository, identifierRepository, identifierTypeName);
        return identifier != null ? identifier.getPerson() : null;
    }

    @Transient
    public String getProxyForPersonUid(IdentifierTypeRepository identifierTypeRepository, IdentifierRepository identifierRepository, String identifierTypeName) {
        Person proxyForPerson = getProxyForPerson(identifierTypeRepository, identifierRepository, identifierTypeName);
        return proxyForPerson != null ? proxyForPerson.getUid() : null;
    }

    private static final int HCB_INIT_ODDRAND = 1169883605;
    private static final int HCB_MULT_ODDRAND = -1981463395;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, delegateProxyType, sourceProxyId, delegateProxySorObjectId, delegateProxySecurityKey, proxyForId
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
        if (obj instanceof DelegateProxy) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((DelegateProxy) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(DelegateProxy obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((DelegateProxy) obj).getHashCodeObjects());
    }

    private void notifyPerson() {
        if (person != null) {
            person.notifyChange(person.getDelegations());
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
                originalPerson.notifyChange(originalPerson.getDelegations());
            }
        }
    }

    public DelegateProxyType getDelegateProxyType() {
        return delegateProxyType;
    }

    public void setDelegateProxyType(DelegateProxyType delegateProxyType) {
        boolean changed = !Objects.equals(delegateProxyType, this.delegateProxyType);
        this.delegateProxyType = delegateProxyType;
        if (changed) notifyPerson();
    }

    public String getSourceProxyId() {
        return sourceProxyId;
    }

    public void setSourceProxyId(String sourceProxyId) {
        boolean changed = !Objects.equals(sourceProxyId, this.sourceProxyId);
        this.sourceProxyId = sourceProxyId;
        if (changed) notifyPerson();
    }

    @Transient
    public Long getDelegateProxySorObjectId() {
        return delegateProxySorObjectId;
    }

    public SORObject getDelegateProxySorObject() {
        return delegateProxySorObject;
    }

    public void setDelegateProxySorObject(SORObject delegateProxySorObject) {
        boolean changed = !Objects.equals(delegateProxySorObject, this.delegateProxySorObject);
        this.delegateProxySorObject = delegateProxySorObject;
        this.delegateProxySorObjectId = delegateProxySorObject != null ? delegateProxySorObject.getId() : null;
        if (changed) notifyPerson();
    }

    public String getDelegateProxySecurityKey() {
        return delegateProxySecurityKey;
    }

    public void setDelegateProxySecurityKey(String delegateProxySecurityKey) {
        boolean changed = !Objects.equals(delegateProxySecurityKey, this.delegateProxySecurityKey);
        this.delegateProxySecurityKey = delegateProxySecurityKey;
        if (changed) notifyPerson();
    }

    public String getProxyForId() {
        return proxyForId;
    }

    public void setProxyForId(String proxyForId) {
        boolean changed = !Objects.equals(proxyForId, this.proxyForId);
        this.proxyForId = proxyForId;
        if (changed) notifyPerson();
    }
}
