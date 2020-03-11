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

import edu.berkeley.bidms.app.registryModel.model.PersonRoleArchive;
import edu.berkeley.bidms.registryModel.util.DateUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Validator for {@link PersonRoleArchive}.
 */
public class PersonRoleArchiveValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PersonRoleArchive.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PersonRoleArchive pra = (PersonRoleArchive) target;
        validateStartOfRoleGraceTime(pra, errors);
        validateEndOfRoleGraceTime(pra, errors);
        validateEndOfRoleGraceTimeOverride(pra, errors);
        validateRoleInGrace(pra, errors);
        validateRolePostGrace(pra, errors);
    }

    private Date dateAddDays(Date date, int days) {
        LocalDateTime ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return Date.from(ldt.plusDays(days).atZone(ZoneId.systemDefault()).toInstant());

    }

    protected void validateStartOfRoleGraceTime(PersonRoleArchive pra, Errors errors) {
        if (pra.getStartOfRoleGraceTime() == null) {
            errors.rejectValue("startOfRoleGraceTime", "startOfRoleGraceTime can't be null");
        }
        // startOfRoleGraceTime can't be in the future
        if (DateUtil.greaterThan(pra.getStartOfRoleGraceTime(), dateAddDays(new Date(), 1))) {
            errors.rejectValue("startOfRoleGraceTime", "startOfRoleGraceTime can't be set to a future date");
        }
        // startOfRoleGraceTime can't be later than endOfRoleGraceTimeUseOverrideIfLater if it's not null
        if (pra.getEndOfRoleGraceTimeUseOverrideIfLater() != null && DateUtil.greaterThan(pra.getStartOfRoleGraceTime(), pra.getEndOfRoleGraceTimeUseOverrideIfLater())) {
            errors.rejectValue("startOfRoleGraceTime", "startOfRoleGraceTime can't be set to a value later than endOfRoleGraceTimeUseOverrideIfLater");
        }
    }

    protected void validateEndOfRoleGraceTime(PersonRoleArchive pra, Errors errors) {
        // if not null, endOfRoleGraceTime can't be earlier than startOfRoleGraceTime
        if (pra.getEndOfRoleGraceTime() != null && DateUtil.lessThan(pra.getEndOfRoleGraceTime(), pra.getStartOfRoleGraceTime())) {
            errors.rejectValue("endOfRoleGraceTime", "endOfRoleGraceTime can't be set to a value earlier than startOfRoleGraceTime");
        }
    }

    protected void validateEndOfRoleGraceTimeOverride(PersonRoleArchive pra, Errors errors) {
        // if not null, endOfRoleGraceTimeOverride can't be earlier than startOfRoleGraceTime
        if (pra.getEndOfRoleGraceTimeOverride() != null && DateUtil.lessThan(pra.getEndOfRoleGraceTimeOverride(), pra.getStartOfRoleGraceTime())) {
            errors.rejectValue("endOfRoleGraceTimeOverride", "endOfRoleGraceTimeOverride can't be set to a value earlier than startOfRoleGraceTime");
        }
    }

    protected void validateRoleInGrace(PersonRoleArchive pra, Errors errors) {
        // one and only one of roleInGrace or rolePostGrace must be true
        if (pra.isRoleInGrace() == pra.isRolePostGrace()) {
            errors.rejectValue("roleInGrace", "roleInGrace and rolePostGrace can't both be " + pra.isRoleInGrace() + ": one and only one must be true");
        }
        // roleInGrace can't be true if the endOfRoleGraceTimeUseOverrideIfLater, if it's not null, is in the past
        if (pra.isRoleInGrace() && pra.getEndOfRoleGraceTimeUseOverrideIfLater() != null && DateUtil.lessThan(pra.getEndOfRoleGraceTimeUseOverrideIfLater(), dateAddDays(new Date(), -1))) {
            errors.rejectValue("roleInGrace", "roleInGrace can't be true if endOfRoleGraceTimeUseOverrideIfLater is in the past, indicating post-grace");
        }
    }

    protected void validateRolePostGrace(PersonRoleArchive pra, Errors errors) {
        // one and only one of roleInGrace or rolePostGrace must be true
        if (pra.isRolePostGrace() == pra.isRoleInGrace()) {
            errors.rejectValue("rolePostGrace", "roleInGrace and rolePostGrace can't both be " + pra.isRolePostGrace() + ": one and only one must be true");
        }
        // rolePostGrace can't be true if endOfRoleGraceTimeUseOverrideIfLater, if it's not null, is in the future
        if (pra.isRolePostGrace() && pra.getEndOfRoleGraceTimeUseOverrideIfLater() != null && DateUtil.greaterThan(pra.getEndOfRoleGraceTimeUseOverrideIfLater(), dateAddDays(new Date(), 1))) {
            errors.rejectValue("rolePostGrace", "rolePostGrace can't be true if endOfRoleGraceTimeUseOverrideIfLater is in the future, indicating in-grace");
        }
    }
}
