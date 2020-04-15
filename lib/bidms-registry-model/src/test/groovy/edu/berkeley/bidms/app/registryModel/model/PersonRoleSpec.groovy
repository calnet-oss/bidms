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


import edu.berkeley.bidms.app.registryModel.repo.AssignableRoleCategoryRepository
import edu.berkeley.bidms.app.registryModel.repo.AssignableRoleRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class PersonRoleSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    AssignableRoleCategoryRepository assignableRoleCategoryRepository

    @Autowired
    AssignableRoleRepository assignableRoleRepository

    @Autowired
    PersonRoleRepository personRoleRepository

    Class<?> getDomainClass() { return PersonRole }

    void "confirm PersonRole is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
        new AssignableRoleCategorySpec(repository: assignableRoleCategoryRepository).insert()
        new AssignableRoleSpec(repository: assignableRoleRepository, assignableRoleCategoryRepository: assignableRoleCategoryRepository).insert()
    }

    void "save test"() {
        when:
        Person person = personRepository.get("1")
        assignableRoleRepository.findAll().each { AssignableRole assignableRole ->
            person.addToAssignedRoles(new PersonRole(
                    role: assignableRole,
                    roleCategory: assignableRole.roleCategory,
                    roleAsgnUniquePerCat: assignableRole.roleCategory.roleAsgnUniquePerCat
            ))
            personRepository.saveAndFlush(person)
        }

        then:
        assignableRoleRepository.findAll().each { AssignableRole assignableRole ->
            assert personRoleRepository.findByPersonAndRole(person, assignableRole)
        }
    }
}
