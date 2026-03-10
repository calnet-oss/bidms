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
package edu.berkeley.bidms.app.registryModel.model

import edu.berkeley.bidms.app.registryModel.model.type.DelegateProxyTypeEnum
import edu.berkeley.bidms.app.registryModel.repo.DelegateProxyTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import spock.lang.Ignore

// ignored because there are no "generic" delegate proxy types yet
@Ignore
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class DelegateProxyTypeSpec extends AbstractTypeSpec<DelegateProxyType, DelegateProxyTypeEnum, Integer, DelegateProxyTypeRepository> {

    @Autowired
    DelegateProxyTypeRepository repository

    Class<?> getDomainClass() { return DelegateProxyType }

    void "confirm DelegateProxyType is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static final String[] delegateProxyTypeNames = []

    String[] getTypeNames() {
        return delegateProxyTypeNames
    }

    DelegateProxyType findByTypeName(String typeName) {
        return repository.findByDelegateProxyTypeName(typeName)
    }

    String getTypeName(DelegateProxyType type) {
        return type.delegateProxyTypeName
    }

    DelegateProxyType newInstance(String typeName) {
        return new DelegateProxyType(delegateProxyTypeName: typeName)
    }

    DelegateProxyTypeEnum getEnum(DelegateProxyType t) {
        return DelegateProxyTypeEnum.getEnum(t)
    }
}
