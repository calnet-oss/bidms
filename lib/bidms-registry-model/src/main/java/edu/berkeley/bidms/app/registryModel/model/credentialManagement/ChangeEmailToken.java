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
package edu.berkeley.bidms.app.registryModel.model.credentialManagement;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.berkeley.bidms.registryModel.util.EntityUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * A token used for recovery email address verification.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
public class ChangeEmailToken extends BaseToken implements Comparable<ChangeEmailToken> {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ChangeEmailToken_seqgen")
    @SequenceGenerator(name = "ChangeEmailToken_seqgen", sequenceName = "ChangeEmailToken_seq", allocationSize = 1)
    @Id
    private Long id;

    @Size(max = 255)
    @NotNull
    @Email
    @Column(nullable = false, length = 255)
    private String emailAddress;

    @PrePersist
    @PreUpdate
    protected void validate() {
        Set<ConstraintViolation<ChangeEmailToken>> set = Validation.buildDefaultValidatorFactory().getValidator().validate(this);
        if (set != null && set.size() > 0) {
            throw new ConstraintViolationException(set);
        }
    }

    private static final int HCB_INIT_ODDRAND = 1359313075;
    private static final int HCB_MULT_ODDRAND = -1194661647;

    private Object[] getHashCodeObjects() {
        return new Object[]{
                getUid(), getToken(), getExpiryDate(), emailAddress
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
        if (obj instanceof ChangeEmailToken) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((ChangeEmailToken) obj).getHashCodeObjects());
        }
        return false;
    }

    @Override
    public int compareTo(ChangeEmailToken obj) {
        return EntityUtil.compareTo(this, getHashCodeObjects(), obj, ((ChangeEmailToken) obj).getHashCodeObjects());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
