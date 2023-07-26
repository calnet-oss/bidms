/*
 * Copyright (c) 2023, Regents of the University of California and
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
package edu.berkeley.bidms.app.registryModel.model


import edu.berkeley.bidms.app.registryModel.model.type.TypeEnum
import edu.berkeley.bidms.app.registryModel.repo.SORTokenTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class SORTokenTypeSpec extends AbstractTypeSpec<SORTokenType, SORTokenTypeEnum, Integer, SORTokenTypeRepository> {

    @Autowired
    SORTokenTypeRepository repository

    Class<?> getDomainClass() { return SORTokenType }

    void "confirm SORTokenType is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static final String[] tokenTypeNames = [
            SORTokenTypeEnum.testToken.name
    ]

    String[] getTypeNames() {
        return tokenTypeNames
    }

    SORTokenType findByTypeName(String typeName) {
        return repository.findByTokenTypeName(typeName)
    }

    String getTypeName(SORTokenType type) {
        return type.tokenTypeName
    }

    SORTokenType newInstance(String typeName) {
        return new SORTokenType(tokenTypeName: typeName)
    }

    SORTokenTypeEnum getEnum(SORTokenType t) {
        return SORTokenTypeEnum.getEnum(t)
    }

    static enum SORTokenTypeEnum implements TypeEnum<SORTokenType, SORTokenTypeRepository> {
        testToken;

        SORTokenType get(SORTokenTypeRepository repo) {
            SORTokenType tokenType = repo.findByTokenTypeName(name());
            if (tokenType == null) {
                throw new RuntimeException("SORTokenType ${name()} could not be found");
            }
            return tokenType;
        }

        String getName() {
            return name();
        }

        Integer getId(SORTokenTypeRepository repo) {
            return get(repo).getId();
        }

        static SORTokenTypeEnum getEnum(SORTokenType t) {
            return valueOf(SORTokenTypeEnum.class, t.tokenTypeName);
        }
    }
}
