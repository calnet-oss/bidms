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
package edu.berkeley.bidms.app.registryModel.model.validator;

import edu.berkeley.bidms.app.registryModel.model.Person;
import edu.berkeley.bidms.app.registryModel.model.PersonRoleArchive;
import edu.berkeley.bidms.registryModel.util.DateUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.validation.ValidationException;
import java.util.Date;
import java.util.Objects;

/**
 * Validator for {@link Person}.
 */
public class PersonValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Person.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Person person = (Person) target;
        validateArchivedRoles(person, errors);
        validateAssignedRoles(person, errors);
    }

    protected void validateAssignedRoles(Person person, Errors errors) {
        person.getAssignedRoles().forEach(role -> {
            if (person.getArchivedRoles().stream().anyMatch(archivedRole ->
                    archivedRole.isRoleAsgnUniquePerCat() && Objects.equals(archivedRole.getRoleCategory(), role.getRoleCategory())
            )) {
                throw new ValidationException("Uid " + person.getUid() + " can't have role " + role.getRole().getRoleName() + " as an assignedRole because a role with the same roleCategory exists as an archivedRole.  Remove the role with roleCategoryId=" + role.getRoleCategory().getId() + " from archiveRoles first, using removeFromArchivedRoles().");
            }
        });

        // the second iteration on the same collection is on purpose so that
        // we consistently fail on any unique-only categories before we fail
        // on any roleIds
        person.getAssignedRoles().forEach(role -> {
            if (person.getArchivedRoles().stream().anyMatch(archivedRole ->
                    Objects.equals(archivedRole.getRole(), role.getRole())
            )) {
                throw new ValidationException("Uid " + person.getUid() + " can't have role " + role.getRole().getRoleName() + " as an assignedRole because a role with the same roleId exists as an archivedRole.  Remove the role with roleId=" + role.getRole().getId() + " from archiveRoles first, using removeFromArchivedRoles().");
            }
        });
    }

    protected void validateArchivedRoles(Person person, Errors errors) {
        Date currentTime = new Date();
        person.getArchivedRoles().forEach(archivedRole -> {
            if (person.getAssignedRoles().stream().anyMatch(role ->
                    role.isRoleAsgnUniquePerCat() && Objects.equals(archivedRole.getRoleCategory(), role.getRoleCategory())
            )) {
                throw new ValidationException("Uid " + person.getUid() + " can't have role " + archivedRole.getRole().getRoleName() + " as an archivedRole because a role with the same roleCategory exists as an assignedRole.  Remove the role with roleCategoryId=" + archivedRole.getRoleCategory().getId() + " from assignedRoles first, using removeFromAssignedRoles().");
            }

            // Flip the in-grace/post-grace booleans, if need be.
            // Necessary, otherwise validation errors could happen when
            // saving the person.
            resetArchivedRoleFlags(currentTime, archivedRole);
        });

        // the second iteration on the same collection is on purpose so that
        // we consistently fail on any unique-only categories before we fail
        // on any roleIds
        person.getArchivedRoles().forEach(archivedRole -> {
            if (person.getAssignedRoles().stream().anyMatch(role ->
                    Objects.equals(role.getRole(), archivedRole.getRole())
            )) {
                throw new ValidationException("Uid " + person.getUid() + " can't have role " + archivedRole.getRole().getRoleName() + " as an archivedRole because a role with the same roleId exists as an assignedRole.  Remove the role with roleId=" + archivedRole.getRole().getId() + " from assignedRoles first, using removeFromAssignedRoles().");
            }
        });
    }

    private void resetArchivedRoleFlags(Date currentTime, PersonRoleArchive archivedRole) {
        archivedRole.setRoleInGrace(archivedRole.getEndOfRoleGraceTimeUseOverrideIfLater() == null || DateUtil.greaterThanEqualsTo(currentTime, archivedRole.getStartOfRoleGraceTime()) && DateUtil.lessThan(currentTime, archivedRole.getEndOfRoleGraceTimeUseOverrideIfLater()));
        archivedRole.setRolePostGrace(archivedRole.getEndOfRoleGraceTimeUseOverrideIfLater() != null && DateUtil.greaterThanEqualsTo(currentTime, archivedRole.getEndOfRoleGraceTimeUseOverrideIfLater()));
    }
}
