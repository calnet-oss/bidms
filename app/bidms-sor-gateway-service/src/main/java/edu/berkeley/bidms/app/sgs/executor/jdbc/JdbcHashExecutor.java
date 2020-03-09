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
package edu.berkeley.bidms.app.sgs.executor.jdbc;

import edu.berkeley.bidms.app.sgs.config.freemarker.FreemarkerConfigurationForSqlTemplates;
import edu.berkeley.bidms.app.sgs.config.properties.SgsConfigProperties;
import edu.berkeley.bidms.app.sgs.executor.HashEntryContent;
import edu.berkeley.bidms.app.sgs.executor.HashExecutorService;
import edu.berkeley.bidms.app.sgs.executor.SorHashExecutor;
import edu.berkeley.bidms.app.sgs.service.RegistryService;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * Execute hash queries for JDBC SORs using a {@link JdbcSearchExecutor}.
 */
@Component
public class JdbcHashExecutor extends SorHashExecutor<Connection, HashEntryContent<Void>> {
    private SgsConfigProperties sgsConfig;
    private FreemarkerConfigurationForSqlTemplates freemarkerConfiguration;
    private HashExecutorService hashExecutorService;

    public JdbcHashExecutor(SgsConfigProperties sgsConfig, RegistryService registryService, FreemarkerConfigurationForSqlTemplates freemarkerConfiguration, HashExecutorService hashExecutorService) {
        super(registryService, hashExecutorService);
        this.sgsConfig = sgsConfig;
        this.freemarkerConfiguration = freemarkerConfiguration;
        this.hashExecutorService = hashExecutorService;
    }

    public JdbcSearchExecutor getSearchExecutor() {
        return new JdbcSearchExecutor(sgsConfig, hashExecutorService, freemarkerConfiguration);
    }
}
