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
package edu.berkeley.bidms.app.registryModel.model

import edu.berkeley.bidms.app.registryModel.model.type.DownstreamSystemEnum
import edu.berkeley.bidms.app.registryModel.repo.DownstreamSystemRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class DownstreamSystemSpec extends AbstractTypeSpec<DownstreamSystem, DownstreamSystemEnum, Integer, DownstreamSystemRepository> {

    @Autowired
    DownstreamSystemRepository repository

    Class<?> getDomainClass() { return DownstreamSystem }

    void "confirm DownstreamSystem is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static final String[] systemNames = DownstreamSystemEnum.values()*.name

    String[] getTypeNames() {
        return systemNames
    }

    DownstreamSystem findByTypeName(String typeName) {
        return repository.findByName(typeName)
    }

    String getTypeName(DownstreamSystem type) {
        return type.name
    }

    DownstreamSystem newInstance(String typeName) {
        return new DownstreamSystem(name: typeName)
    }

    DownstreamSystemEnum getEnum(DownstreamSystem t) {
        return DownstreamSystemEnum.getEnum(t)
    }

    static void insertSystemNames(DownstreamSystemRepository repo) {
        new DownstreamSystemSpec(repository: repo).insert()
    }

    void "test type name list is as expected"() {
        expect:
        (typeNames as List<String>).sort() == [
                "LDAP", "AD"
        ].sort()
    }
}
