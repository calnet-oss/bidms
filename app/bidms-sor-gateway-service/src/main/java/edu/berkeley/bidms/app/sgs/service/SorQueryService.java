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

import edu.berkeley.bidms.app.restservice.common.response.NotFoundException;
import edu.berkeley.bidms.app.sgs.config.properties.QueryExecutorConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.SgsConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.SorConfigProperties;
import edu.berkeley.bidms.app.sgs.executor.QueryExecutorException;
import edu.berkeley.bidms.app.sgs.executor.SorQueryExecutor;
import edu.berkeley.bidms.app.sgs.model.request.SorQueryRequest;
import edu.berkeley.bidms.app.sgs.model.response.SorQueryResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Perform query operations.
 */
@Service
public class SorQueryService implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private SgsConfigProperties configProperties;

    public SorQueryService(SgsConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Perform a query.
     *
     * @param cmd A {@link SorQueryRequest} that contains particulars about
     *            the request such as which SOR to query.
     * @return A {@link SorQueryResponse} that contains information about the
     * query result.
     * @throws NotFoundException      If the SOR does not exist.
     * @throws QueryExecutorException If an error occurred.
     */
    public SorQueryResponse query(SorQueryRequest cmd) throws NotFoundException, QueryExecutorException {
        // Get configuration for the SOR
        SorConfigProperties sorConfig = configProperties.getSors().get(cmd.getSorName());
        if (sorConfig == null) {
            throw new NotFoundException(cmd.getSorName() + " SOR not configured");
        }

        // Get configuration for the query executor specified in the SOR configuration
        QueryExecutorConfigProperties queryExecutorConfig = configProperties.getQueryExecutors().get(sorConfig.getQueryExecutorName());
        if (queryExecutorConfig == null) {
            throw new NotFoundException(sorConfig.getQueryExecutorName() + " query executor is not configured");
        }

        // Get the query executor bean by the bean name specified in the query executor configuration
        SorQueryExecutor<?, ?> queryExecutor = applicationContext.getBean(queryExecutorConfig.getBeanName(), SorQueryExecutor.class);

        // Do the querying
        return queryExecutor.query(sorConfig, cmd);
    }
}
