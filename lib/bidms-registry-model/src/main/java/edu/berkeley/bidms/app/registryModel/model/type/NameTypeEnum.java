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

import edu.berkeley.bidms.app.registryModel.model.NameType;
import edu.berkeley.bidms.app.registryModel.repo.NameTypeRepository;

/**
 * The different types of names from SOR JSON that translate into a
 * NameType.
 */
public enum NameTypeEnum implements TypeEnum<NameType, NameTypeRepository> {
    sorPrimaryName,
    sorPreferredName;

    public NameType get(NameTypeRepository repo) {
        NameType nameType = repo.findByTypeName(name());
        if (nameType == null) {
            throw new RuntimeException("NameType " + name() + " could not be found");
        }
        return nameType;
    }

    public String getName() {
        return name();
    }

    public Integer getId(NameTypeRepository repo) {
        return get(repo).getId();
    }

    public static NameTypeEnum getEnum(NameType t) {
        return valueOf(NameTypeEnum.class, t.getTypeName());
    }
}
