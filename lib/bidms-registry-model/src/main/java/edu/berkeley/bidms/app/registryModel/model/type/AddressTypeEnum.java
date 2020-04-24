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
package edu.berkeley.bidms.app.registryModel.model.type;

import edu.berkeley.bidms.app.registryModel.model.AddressType;
import edu.berkeley.bidms.app.registryModel.repo.AddressTypeRepository;

/**
 * The different types of addresses from SOR JSON that translate into a
 * AddressType.
 */
public enum AddressTypeEnum implements TypeEnum<AddressType, AddressTypeRepository> {
    directoryPrimaryAddress,
    directorySecondaryAddress;

    public AddressType get(AddressTypeRepository repo) {
        AddressType addressType = repo.findByAddressTypeName(name());
        if (addressType == null) {
            throw new RuntimeException("AddressType " + name() + " could not be found");
        }
        return addressType;
    }

    public String getName() {
        return name();
    }

    public Integer getId(AddressTypeRepository repo) {
        return get(repo).getId();
    }

    public static AddressTypeEnum getEnum(AddressType t) {
        return valueOf(AddressTypeEnum.class, t.getAddressTypeName());
    }
}
