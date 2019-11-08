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
package edu.berkeley.bidms.app.registryModel.model.type;

import edu.berkeley.bidms.app.registryModel.model.AssignableRole;
import edu.berkeley.bidms.app.registryModel.repo.AssignableRoleRepository;

/**
 * Roles that can be assigned to a person.
 */
public enum AssignableRoleEnum implements TypeEnum<AssignableRole, AssignableRoleRepository> {
    /**
     * primaryOU roles (one-per-person)
     */
    ouPeople,
    ouPresir,
    ouGuests,
    ouAdvcon,
    ouExpired,

    /**
     * Account master status flag
     */
    masterAccountActive,
    /**
     * Similar to masterAccountActive except it disregards certain SOR roles,
     * such as ADVCON, when determining account status by rolling-up all the
     * other SOR roles into this set-the-exp-date-or-not role.  It's used in
     * logic where where some SOR records, such as ADVCON are active, but
     * should not prevent berkeleyEduExpDate from being set.
     * <p>
     * In other words, if this role is active, then don't set
     * berkeleyEduExpDate.  If this role is archived (inactive), then set
     * berkeleyEduExpDate.
     */
    ldapNoExpDate,
    /* Eligible to receive grace/expiration email notifications */
    expirationNotify;

    public AssignableRole get(AssignableRoleRepository repo) {
        AssignableRole role = repo.findByRoleName(name());
        if (role == null) {
            throw new RuntimeException("AssignableRole " + name() + " could not be found");
        }
        return role;
    }

    public String getName() {
        return name();
    }

    public Integer getId(AssignableRoleRepository repo) {
        return get(repo).getId();
    }

    public static AssignableRoleEnum getEnum(AssignableRole r) {
        return valueOf(AssignableRoleEnum.class, r.getRoleName());
    }
}
