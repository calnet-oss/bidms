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

import edu.berkeley.bidms.app.registryModel.model.type.AssignableRoleCategoryEnum
import edu.berkeley.bidms.app.registryModel.model.type.AssignableRoleEnum
import edu.berkeley.bidms.app.registryModel.repo.AssignableRoleCategoryRepository
import edu.berkeley.bidms.app.registryModel.repo.AssignableRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class AssignableRoleSpec extends AbstractTypeSpec<AssignableRole, AssignableRoleEnum, Integer, AssignableRoleRepository> {

    @Autowired
    AssignableRoleRepository repository

    @Autowired
    AssignableRoleCategoryRepository assignableRoleCategoryRepository

    Class<?> getDomainClass() { return AssignableRole }

    void "confirm AssignableRole is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static final String[] assignableRoleNames = [
            AssignableRoleEnum.ouPeople.name,
            AssignableRoleEnum.ouPresir.name,
            AssignableRoleEnum.ouGuests.name,
            AssignableRoleEnum.ouAdvcon.name,
            AssignableRoleEnum.ouExpired.name,
            AssignableRoleEnum.masterAccountActive.name
    ]

    void setup() {
        new AssignableRoleCategorySpec(repository: assignableRoleCategoryRepository).insert()
    }

    String[] getTypeNames() {
        return assignableRoleNames
    }

    AssignableRole findByTypeName(String typeName) {
        return repository.findByRoleName(typeName)
    }

    String getTypeName(AssignableRole type) {
        return type.roleName
    }

    static AssignableRoleCategory getAssignableRoleCategoryForRoleName(AssignableRoleCategoryRepository repo, String roleName) {
        if (roleName.startsWith("ou"))
            return repo.findByCategoryName(AssignableRoleCategoryEnum.primaryOU.name)
        else if (roleName.startsWith("ldapAffil"))
            return repo.findByCategoryName(AssignableRoleCategoryEnum.ldapAffiliation.name)
        else if (roleName.startsWith("masterAccount"))
            return repo.findByCategoryName(AssignableRoleCategoryEnum.masterAccountStatus.name)
        else
            throw new RuntimeException("Unknown category for assignable role name ${roleName}")
    }

    AssignableRole newInstance(String typeName) {
        AssignableRoleCategory roleCategory = getAssignableRoleCategoryForRoleName(assignableRoleCategoryRepository, typeName)
        assert typeName && roleCategory
        return new AssignableRole(
                roleName: typeName,
                roleCategory: roleCategory
        )
    }

    AssignableRoleEnum getEnum(AssignableRole t) {
        return AssignableRoleEnum.getEnum(t)
    }
}
