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
package edu.berkeley.bidms.app.registryModel.model.type;

import edu.berkeley.bidms.app.registryModel.model.DelegateProxyType;
import edu.berkeley.bidms.app.registryModel.repo.DelegateProxyTypeRepository;

public enum DelegateProxyTypeEnum implements TypeEnum<DelegateProxyType, DelegateProxyTypeRepository> {
    /* No "generic" delegate proxy types yet */;

    private IdentifierTypeEnum identifierTypeEnum;

    DelegateProxyTypeEnum(IdentifierTypeEnum identifierTypeEnum) {
        this.identifierTypeEnum = identifierTypeEnum;
    }

    public DelegateProxyType get(DelegateProxyTypeRepository repo) {
        DelegateProxyType delegateProxyType = repo.findByDelegateProxyTypeName(name());
        if (delegateProxyType == null) {
            throw new RuntimeException("DelegateProxyType " + name() + " could not be found");
        }
        return delegateProxyType;
    }

    public String getName() {
        return name();
    }

    public Integer getId(DelegateProxyTypeRepository repo) {
        return get(repo).getId();
    }

    public static DelegateProxyTypeEnum getEnum(DelegateProxyType pt) {
        return valueOf(DelegateProxyTypeEnum.class, pt.getDelegateProxyTypeName());
    }

    public IdentifierTypeEnum getIdentifierTypeEnum() {
        return identifierTypeEnum;
    }
}
