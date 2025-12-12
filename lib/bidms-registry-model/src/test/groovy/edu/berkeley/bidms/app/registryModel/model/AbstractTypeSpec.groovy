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
package edu.berkeley.bidms.app.registryModel.model

import edu.berkeley.bidms.app.registryModel.model.type.TypeEnum
import edu.berkeley.bidms.registryModel.repo.ExtendedRepository
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
abstract class AbstractTypeSpec<T, EN extends TypeEnum, I, R extends ExtendedRepository<T, I>> extends Specification {
    abstract R getRepository()

    abstract String[] getTypeNames()

    abstract T findByTypeName(String typeName)

    abstract String getTypeName(T type)

    Iterable<T> list() {
        return repository.findAll()
    }

    abstract T newInstance(String typeName)

    abstract EN getEnum(T t)

    void insert() {
        for (String typeName in typeNames) {
            T type = newInstance(typeName)
            repository.saveAndFlush(type)
        }
    }

    void "save test"() {
        when:
        insert()

        then:
        typeNames.each { typeName ->
            assert getTypeName(findByTypeName(typeName)) == typeName
        }
    }

    void "test type enum"() {
        when:
        insert()

        then:
        typeNames.each { typeName ->
            T type = findByTypeName(typeName)
            assert typeName && getTypeName(type) == typeName
            // test getEnum() and get() and getId()
            //EN en = getEnum(type)
            //assert typeName && en && en.get(repository) == type && en.get(repository).id == type.id
        }
    }
}
