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

import edu.berkeley.bidms.app.registryModel.model.PronounType;
import edu.berkeley.bidms.app.registryModel.repo.PronounTypeRepository;

/**
 * The different types of pronouns from SOR JSON that translates into a
 * PronounType
 */
public enum PronounTypeEnum implements TypeEnum<PronounType, PronounTypeRepository> {
    /* no "generic" pronoun types yet */;

    public PronounType get(PronounTypeRepository repo) {
        PronounType pronounType = repo.findByPronounTypeName(name());
        if (pronounType == null) {
            throw new RuntimeException("PronounType ${name()} could not be found");
        }
        return pronounType;
    }

    public String getName() {
        return name();
    }

    public Integer getId(PronounTypeRepository repo) {
        return get(repo).getId();
    }

    public static PronounTypeEnum getEnum(PronounType t) {
        return valueOf(PronounTypeEnum.class, t.getPronounTypeName());
    }
}
