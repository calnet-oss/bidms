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
package edu.berkeley.bidms.app.sgs.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

/**
 * Operations performed on the Registry database.
 */
@Service
public class RegistryService {
    private JdbcTemplate registryJdbcTemplate;
    private RegistrySqlTextService registrySqlTextService;

    public RegistryService(JdbcTemplate registryJdbcTemplate, RegistrySqlTextService registrySqlTextService) {
        this.registryJdbcTemplate = registryJdbcTemplate;
        this.registrySqlTextService = registrySqlTextService;
    }

    /**
     * Look up a sorId for a sorName.
     *
     * @param sorName The sorName to query for.
     * @return If sorName is found, return the sorId.  If sorName not found, returns null.
     */
    public Integer getSorId(String sorName) {
        SqlRowSet rowSet = registryJdbcTemplate.queryForRowSet(registrySqlTextService.sorIdBySorNameSql("SELECT sorId FROM SOR WHERE sorName=?"), sorName);
        if (rowSet.next()) {
            return rowSet.getInt(1);
        }
        return null;
    }
}
