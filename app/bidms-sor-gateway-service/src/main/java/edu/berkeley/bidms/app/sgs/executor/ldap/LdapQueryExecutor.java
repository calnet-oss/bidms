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
package edu.berkeley.bidms.app.sgs.executor.ldap;

import edu.berkeley.bidms.app.sgs.config.properties.SgsConfigProperties;
import edu.berkeley.bidms.app.sgs.executor.QueryExecutorService;
import edu.berkeley.bidms.app.sgs.executor.SorQueryExecutor;
import edu.berkeley.bidms.app.sgs.service.RegistryService;
import org.springframework.stereotype.Component;

/**
 * Execute queries for LDAP directory SORs using a {@link
 * LdapSearchExecutor}.
 */
@Component
public class LdapQueryExecutor extends SorQueryExecutor<LdapTemplateExt, LdapQueryEntryContent> {
    private SgsConfigProperties sgsConfigProperties;
    private QueryExecutorService queryExecutorService;

    public LdapQueryExecutor(SgsConfigProperties sgsConfigProperties, RegistryService registryService, QueryExecutorService queryExecutorService) {
        super(registryService, queryExecutorService);
        this.sgsConfigProperties = sgsConfigProperties;
        this.queryExecutorService = queryExecutorService;
    }

    @Override
    protected LdapSearchExecutor getSearchExecutor() {
        return new LdapSearchExecutor(sgsConfigProperties, queryExecutorService);
    }
}
