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

import edu.berkeley.bidms.app.registryModel.repo.IdentifierArchiveRepository
import edu.berkeley.bidms.app.registryModel.repo.IdentifierTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import jakarta.persistence.Entity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class IdentifierArchiveSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    IdentifierTypeRepository identifierTypeRepository

    @Autowired
    IdentifierArchiveRepository identifierArchiveRepository

    Class<?> getDomainClass() { return IdentifierArchive }

    void "confirm IdentifierArchive is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    void setup() {
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
    }

    void "test read only"() {
        when:
        IdentifierType identifierType = identifierTypeRepository.save(new IdentifierType(idName: "hr"))
        identifierArchiveRepository.save(new IdentifierArchive(
                id: 1,
                person: personRepository.get("1"),
                identifierType: identifierType,
                originalSorObjectId: 123,
                identifier: "bogus",
                wasActive: false,
                wasPrimary: false,
                oldWeight: 0
        ))

        then:
        Exception ex = thrown()
        ex.message == "IdentifierArchive is read-only"
    }

    @Entity
    static class IdentifierArchiveWriteable extends IdentifierArchive {
        @Override
        protected void enforceReadOnly() {
        }
    }

    void "test overriding read only"() {
        when:
        IdentifierType identifierType = identifierTypeRepository.save(new IdentifierType(idName: "hr"))
        IdentifierArchiveWriteable obj = new IdentifierArchiveWriteable(
                id: 1,
                person: personRepository.get("1"),
                identifierType: identifierType,
                originalSorObjectId: 123,
                identifier: "bogus",
                wasActive: false,
                wasPrimary: false,
                oldWeight: 0,
        )
        IdentifierArchive identifier = identifierArchiveRepository.save(obj)

        then:
        identifier
    }
}
