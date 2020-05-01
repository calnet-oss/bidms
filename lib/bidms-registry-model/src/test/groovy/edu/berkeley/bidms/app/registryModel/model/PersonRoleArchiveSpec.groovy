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

import edu.berkeley.bidms.app.registryModel.model.type.AssignableRoleEnum
import edu.berkeley.bidms.app.registryModel.repo.AssignableRoleCategoryRepository
import edu.berkeley.bidms.app.registryModel.repo.AssignableRoleRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRoleArchiveRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import edu.berkeley.bidms.orm.event.EventValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static edu.berkeley.bidms.app.registryModel.model.type.AssignableRoleEnum.masterAccountActive
import static edu.berkeley.bidms.app.registryModel.model.type.AssignableRoleEnum.ouExpired
import static edu.berkeley.bidms.app.registryModel.model.type.AssignableRoleEnum.ouPeople

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class PersonRoleArchiveSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    AssignableRoleCategoryRepository assignableRoleCategoryRepository

    @Autowired
    AssignableRoleRepository assignableRoleRepository

    @Autowired
    PersonRoleArchiveRepository personRoleArchiveRepository

    Class<?> getDomainClass() { return PersonRoleArchive }

    static long fiveMinuteOfMilliseconds = 5 * 60 * 1000

    @Shared
    static final Date now = new Date()

    @Shared
    static Date earlier = new Date(now.time - fiveMinuteOfMilliseconds)

    @Shared
    static Date later = new Date(now.time + fiveMinuteOfMilliseconds)

    void "confirm PersonRoleArchive is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    void setup() {
        PersonSpec.insertPeople(personRepository)
        new AssignableRoleCategorySpec(repository: assignableRoleCategoryRepository).insert()
        new AssignableRoleSpec(repository: assignableRoleRepository, assignableRoleCategoryRepository: assignableRoleCategoryRepository).insert()
    }

    @Unroll
    void "test getEndOfRoleGraceTimeUseOverrideIfLater"() {
        when:
        PersonRoleArchive pra = new PersonRoleArchive(
                endOfRoleGraceTime: endOfRoleGraceTime,
                endOfRoleGraceTimeOverride: endOfRoleGraceTimeOverride
        )

        then:
        pra.endOfRoleGraceTimeUseOverrideIfLater == expectedEndOfRoleGraceTimeUseOverrideIfLater

        where:
        endOfRoleGraceTime | endOfRoleGraceTimeOverride | expectedEndOfRoleGraceTimeUseOverrideIfLater
        null               | null                       | null
        null               | now                        | now
        now                | null                       | now
        now                | now                        | now
        now                | earlier                    | now
        now                | later                      | later
    }

    void "save test"() {
        when:
        Person person = personRepository.get("1")
        assignableRoleRepository.findAll().each { AssignableRole assignableRole ->
            def pra = new PersonRoleArchive(person)
            pra.with {
                role = assignableRole
                roleCategory = assignableRole.roleCategory
                roleAsgnUniquePerCat = assignableRole.roleCategory.roleAsgnUniquePerCat
                originalTimeCreated = now
                originalTimeUpdated = now
                rolePostGrace = true
                roleInGrace = false
                startOfRoleGraceTime = now
            }
            person.addToArchivedRoles(pra)
        }
        personRepository.saveAndFlush(person)

        then:
        assignableRoleRepository.findAll().each { AssignableRole assignableRole ->
            assert personRoleArchiveRepository.findByPersonAndRole(person, assignableRole)
        }
    }

    @Unroll
    void "confirm Person can't be saved with same role in assignedRoles and archivedRoles"() {
        given:
        Person person = personRepository.get("1")

        when:
        EventValidationException exception = null

        assignedRoles?.each { AssignableRoleEnum roleEnum ->
            AssignableRole role = assignableRoleRepository.findByRoleName(roleEnum.name)
            assert role
            def pr = new PersonRole(person)
            pr.with {
                it.role = role
                it.roleCategory = role.roleCategory
                it.roleAsgnUniquePerCat = role.roleCategory.roleAsgnUniquePerCat
            }
            person.addToAssignedRoles(pr)
        }
        archivedRoles?.each { AssignableRoleEnum roleEnum ->
            AssignableRole role = assignableRoleRepository.findByRoleName(roleEnum.name)
            assert role
            def pra = new PersonRoleArchive(person)
            pra.with {
                it.role = role
                it.roleCategory = role.roleCategory
                it.roleAsgnUniquePerCat = role.roleCategory.roleAsgnUniquePerCat
                it.originalTimeCreated = now
                it.originalTimeUpdated = now
                it.rolePostGrace = true
                it.roleInGrace = false
                it.startOfRoleGraceTime = now
            }
            person.addToArchivedRoles(pra)
        }
        try {
            personRepository.saveAndFlush(person)
        }
        catch (EventValidationException e) {
            exception = e
        }
        boolean isArchivedRolesCategoryException = exception?.message?.contains("as an archivedRole because a role with the same roleCategory exists as an assignedRole")
        boolean isArchivedRolesRoleIdException = exception?.message?.contains("as an archivedRole because a role with the same roleId exists as an assignedRole")
        boolean isAssignedRolesCategoryException = exception?.message?.contains("as an assignedRole because a role with the same roleCategory exists as an archivedRole")
        boolean isAssignedRolesRoleIdException = exception?.message?.contains("as an assignedRole because a role with the same roleId exists as an archivedRole")
        // println("isArchivedRolesCategoryException=$isArchivedRolesCategoryException, isArchivedRolesRoleIdException=$isArchivedRolesRoleIdException, isAssignedRolesCategoryException=$isAssignedRolesCategoryException, isAssignedRolesRoleIdException=$isAssignedRolesRoleIdException")

        then:
        isArchivedRolesCategoryException == archivedRolesCategoryExceptionExpected
        isArchivedRolesRoleIdException == archivedRolesRoleIdExceptionExpected
        isAssignedRolesCategoryException == assignedRolesCategoryExceptionExpected
        isAssignedRolesRoleIdException == assignedRolesRoleIdExceptionExpected


        // Exception order:
        // If archivedRole roleCategory check fails, that gets thrown
        // Else if archiveRole roleId check fails, that gets thrown
        // Else if assignedRole roleCategory check fails, that gets thrown
        // Else if assignedRole roleId check fails, that gets thrown
        where:
        assignedRoles                    | archivedRoles                   | archivedRolesCategoryExceptionExpected | archivedRolesRoleIdExceptionExpected | assignedRolesCategoryExceptionExpected | assignedRolesRoleIdExceptionExpected
        []                               | []                              | false                                  | false                                | false                                  | false
        [masterAccountActive]            | []                              | false                                  | false                                | false                                  | false
        []                               | [masterAccountActive]           | false                                  | false                                | false                                  | false
        [masterAccountActive]            | [masterAccountActive]           | false                                  | true                                 | false                                  | false
        [ouExpired]                      | [ouPeople]                      | true                                   | false                                | false                                  | false
        [masterAccountActive, ouExpired] | [masterAccountActive, ouPeople] | true                                   | false                                | false                                  | false
    }

    @Unroll
    void "test begin and end grace time validation"() {
        when:
        Person person = personRepository.get("1")
        Exception exception = null
        assignableRoleRepository.findAll().each { AssignableRole assignableRole ->
            PersonRoleArchive pra = new PersonRoleArchive(person)
            pra.with {
                role = assignableRole
                roleCategory = assignableRole.roleCategory
                roleAsgnUniquePerCat = assignableRole.roleCategory.roleAsgnUniquePerCat
                originalTimeCreated = now
                originalTimeUpdated = now
                startOfRoleGraceTime = startOfGrace
                endOfRoleGraceTime = endOfGrace
                roleInGrace = isInGrace
                rolePostGrace = isPostGrace
            }
            person.addToArchivedRoles(pra)
        }
        try {
            personRepository.saveAndFlush(person)
        }
        catch (EventValidationException e) {
            exception = e
        }

        then:
        (validationErrorExpected ? exception : !exception)

        where:
        description                                                     | startOfGrace                           | endOfGrace                             | isInGrace | isPostGrace | validationErrorExpected
        "startOfGrace can't be null"                                    | null                                   | null                                   | false     | true        | true
        "startOfGrace can't be in the future"                           | Date.parse("YYYY-MM-dd", "9999-01-01") | null                                   | true      | false       | true
        "good startOfGrace and endOfGrace"                              | Date.parse("YYYY-MM-dd", "1901-01-01") | Date.parse("YYYY-MM-dd", "4000-01-01") | true      | false       | false
        "endOfRoleGraceTime can't be earlier than startOfRoleGraceTime" | now - 4                                | now - 6                                | false     | true        | true
    }

    @Unroll("test rolePostGrace and roleInGrace validation: #description")
    void "test rolePostGrace and roleInGrace validation"() {
        given:
        List<PersonRoleArchive> addedArchivedRoles = []

        when:
        Person person = personRepository.get("1")
        assignableRoleRepository.findAll().each { AssignableRole assignableRole ->
            PersonRoleArchive pra = new PersonRoleArchive(person)
            pra.with {
                role = assignableRole
                roleCategory = assignableRole.roleCategory
                roleAsgnUniquePerCat = assignableRole.roleCategory.roleAsgnUniquePerCat
                originalTimeCreated = now
                originalTimeUpdated = now
                startOfRoleGraceTime = startOfGrace
                endOfRoleGraceTime = endOfGrace
                endOfRoleGraceTimeOverride = endOfGraceOverride
                roleInGrace = isInGrace
                rolePostGrace = isPostGrace
            }
            person.addToArchivedRoles(pra)
        }
        personRepository.saveAndFlush(person)

        then:
        !addedArchivedRoles.any { it.roleInGrace != expectedInGrace || it.rolePostGrace != expectedPostGrace }

        where:
        // Note A: Person validator, resetArchivedRoleFlags() correct the wrong boolean flags during validation, so no validation exception thrown.
        description                                                                       | startOfGrace | endOfGrace | endOfGraceOverride | isInGrace | isPostGrace | expectedInGrace | expectedPostGrace
        "both can't be true" /* Note A */                                                 | now          | null       | null               | true      | true        | true            | false
        "both can't be false" /* Note A */                                                | now          | null       | null               | false     | false       | true            | false
        "good isInGrace"                                                                  | now          | now + 2    | null               | true      | false       | true            | false
        "good isInGrace - start and end is same"                                          | now          | now        | null               | true      | false       | true            | false
        "good isPostGrace"                                                                | now - 5      | now - 3    | null               | false     | true        | false           | true
        "good isPostGrace - start and end is the same"                                    | now          | now        | null               | false     | true        | false           | true
        "isInGrace can't be true when endOfRoleGraceTime is in the past" /* Note A */     | now - 5      | now - 3    | null               | true      | false       | false           | true
        "isInGrace when override in the future but the non-override isn't"                | now - 5      | now - 3    | now + 1            | true      | false       | true            | false
        "isPostGrace can't be true when endOfRoleGraceTime is in the future" /* Note A */ | now          | now + 2    | null               | false     | true        | true            | false
    }
}
