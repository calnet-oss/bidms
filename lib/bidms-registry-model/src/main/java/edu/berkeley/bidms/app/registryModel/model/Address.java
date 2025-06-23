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

import java.util.Objects;

/**
 * A physical location address.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"uid", "person", "sorObject"})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "sorObjectId", "addressTypeId", "address1", "address2", "address3", "city", "regionState", "postalCode", "country", "roomNumber", "mailCode"}))
@Entity
public class Address implements Comparable<Address> {

    protected Address() {
    }

    public Address(Person person) {
        this.person = person;
        this.uid = person != null ? person.getUid() : null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Address_seqgen")
    @SequenceGenerator(name = "Address_seqgen", sequenceName = "Address_seq", allocationSize = 1)
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
    @JoinColumn(name = "addressTypeId", nullable = false)
    private AddressType addressType;

    @Column(insertable = false, updatable = false)
    private Long sorObjectId;

    //@NotNull // TODO in tests
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorObjectId", nullable = false)
    private SORObject sorObject;

    @Size(max = 255)
    @Column(length = 255)
    private String address1;

    @Size(max = 255)
    @Column(length = 255)
    private String address2;

    @Size(max = 255)
    @Column(length = 255)
    private String address3;

    @Size(max = 255)
    @Column(length = 255)
    private String city;

    @Size(max = 255)
    @Column(length = 255)
    private String regionState;

    @Size(max = 64)
    @Column(length = 64)
    private String postalCode;

    @Size(max = 255)
    @Column(length = 255)
    private String country;

    @Size(max = 95)
    @Column(length = 95)
    private String roomNumber;

    @Size(max = 64)
    @Column(length = 64)
    private String mailCode;

    private static final int HCB_INIT_ODDRAND = 1943320795;
    private static final int HCB_MULT_ODDRAND = 88274277;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                uid, addressType, sorObjectId, address1, address2,
                address3, city, regionState, postalCode, country,
                roomNumber, mailCode
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
        if (obj instanceof Address) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((Address) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(Address obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((Address) obj).getHashCodeObjects());
    }

    private void notifyPerson() {
        if (person != null) {
            person.notifyChange(person.getAddresses());
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
                originalPerson.notifyChange(originalPerson.getAddresses());
            }
        }
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        boolean changed = !Objects.equals(addressType, this.addressType);
        this.addressType = addressType;
        if (changed) notifyPerson();
    }

    @Transient
    public Long getSorObjectId() {
        return sorObjectId;
    }

    public SORObject getSorObject() {
        return sorObject;
    }

    public void setSorObject(SORObject sorObject) {
        boolean changed = !Objects.equals(sorObject, this.sorObject) || (sorObject != null && !Objects.equals(sorObject.getId(), sorObjectId));
        this.sorObject = sorObject;
        this.sorObjectId = sorObject != null ? sorObject.getId() : null;
        if (changed) notifyPerson();
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        boolean changed = !Objects.equals(address1, this.address1);
        this.address1 = address1;
        if (changed) notifyPerson();
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        boolean changed = !Objects.equals(address2, this.address2);
        this.address2 = address2;
        if (changed) notifyPerson();
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        boolean changed = !Objects.equals(address3, this.address3);
        this.address3 = address3;
        if (changed) notifyPerson();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        boolean changed = !Objects.equals(city, this.city);
        this.city = city;
        if (changed) notifyPerson();
    }

    public String getRegionState() {
        return regionState;
    }

    public void setRegionState(String regionState) {
        boolean changed = !Objects.equals(regionState, this.regionState);
        this.regionState = regionState;
        if (changed) notifyPerson();
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        boolean changed = !Objects.equals(postalCode, this.postalCode);
        this.postalCode = postalCode;
        if (changed) notifyPerson();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        boolean changed = !Objects.equals(country, this.country);
        this.country = country;
        if (changed) notifyPerson();
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        boolean changed = !Objects.equals(country, this.roomNumber);
        this.roomNumber = roomNumber;
        if (changed) notifyPerson();
    }

    public String getMailCode() {
        return mailCode;
    }

    public void setMailCode(String mailCode) {
        boolean changed = !Objects.equals(country, this.mailCode);
        this.mailCode = mailCode;
        if (changed) notifyPerson();
    }
}
