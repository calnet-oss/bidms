/*
 * Copyright (c) 2020, Regents of the University of California and
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

import edu.berkeley.bidms.app.registryModel.model.PersonRoleArchive;
import edu.berkeley.bidms.registryModel.util.DateUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Date;

/**
 * Validator for {@link PersonRoleArchive} when loaded.
 */
public class PersonRoleArchiveOnLoadValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return PersonRoleArchive.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        resetArchivedRoleFlags(new Date(), (PersonRoleArchive) target);
    }

    static void resetArchivedRoleFlags(Date currentTime, PersonRoleArchive archivedRole) {
        archivedRole.setRoleInGrace(archivedRole.getEndOfRoleGraceTimeUseOverrideIfLater() == null || DateUtil.greaterThanEqualsTo(currentTime, archivedRole.getStartOfRoleGraceTime()) && DateUtil.lessThan(currentTime, archivedRole.getEndOfRoleGraceTimeUseOverrideIfLater()));
        archivedRole.setRolePostGrace(archivedRole.getEndOfRoleGraceTimeUseOverrideIfLater() != null && DateUtil.greaterThanEqualsTo(currentTime, archivedRole.getEndOfRoleGraceTimeUseOverrideIfLater()));
    }
}
