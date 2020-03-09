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
package edu.berkeley.bidms.app.sgs.executor;

import edu.berkeley.bidms.app.sgs.config.properties.QueryEntryExtractorConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.SgsConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.SorConfigProperties;
import org.springframework.context.ApplicationContext;

/**
 * A base class for executor service implementations.
 */
public abstract class ExecutorService {

    private SgsConfigProperties sgsConfig;

    public ExecutorService(SgsConfigProperties sgsConfig) {
        this.sgsConfig = sgsConfig;
    }

    protected abstract ApplicationContext getApplicationContext();

    /**
     * Get the configured {@link EntryContentExtractor} bean for a SOR.
     *
     * @param sorConfig      SOR configuration.
     * @param extractorClass The class of a {@link EntryContentExtractor}
     *                       implementation.
     * @param <T>            The type of object being parsed within the
     *                       {@link EntryContentExtractor}.
     * @param <R>            The type of the resulting object that contains
     *                       the extracted data from the {@link
     *                       EntryContentExtractor}.
     * @param <E>            The type of the {@link EntryContentExtractor}
     *                       implementation.
     * @return The {@link EntryContentExtractor} bean.
     * @throws ExecutorConfigurationException The {@code sorConfig.getQueryEntryExtractorName()}
     *                                        was not found in the {@code
     *                                        sgsConfig.getQueryEntryExtractors()}
     *                                        configuration.
     */
    public <T, R, E extends EntryContentExtractor<T, R>> E getConfiguredQueryEntryExtractorBean(SorConfigProperties sorConfig, Class<E> extractorClass) throws ExecutorConfigurationException {
        if (sorConfig.getQueryEntryExtractorName() != null) {
            QueryEntryExtractorConfigProperties queryEntryExtractorConfig = sgsConfig.getQueryEntryExtractors().get(sorConfig.getQueryEntryExtractorName());
            if (queryEntryExtractorConfig == null) {
                throw new ExecutorConfigurationException(sorConfig.getQueryEntryExtractorName() + " query entry extractor is not configured");
            }
            // Get the query entry extractor bean by the bean name specified in the query entry extractor configuration
            return getApplicationContext().getBean(queryEntryExtractorConfig.getBeanName(), extractorClass);
        }
        return null;
    }
}
