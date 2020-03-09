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

import edu.berkeley.bidms.app.sgs.config.properties.SorConfigProperties;
import edu.berkeley.bidms.app.sgs.model.request.SorHashRequest;
import edu.berkeley.bidms.app.sgs.model.response.SorHashResponse;
import edu.berkeley.bidms.app.sgs.service.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;

/**
 * Perform hash queries on a SOR.
 *
 * @param <C> The context type which is typically something like a connection
 *            to the SOR.
 * @param <H> A type that implements {@link HashEntryContent}.
 */
public abstract class SorHashExecutor<C extends AutoCloseable, H extends HashEntryContent<?>> {
    private final Logger log = LoggerFactory.getLogger(SorHashExecutor.class);

    private RegistryService registryService;
    private HashExecutorService hashExecutorService;

    public SorHashExecutor(RegistryService registryService, HashExecutorService hashExecutorService) {
        this.registryService = registryService;
        this.hashExecutorService = hashExecutorService;
    }

    /**
     * @return An implementation of {@link SearchExecutor}.
     */
    protected abstract SearchExecutor<C, H, ?> getSearchExecutor();

    /**
     * Perform a hash query on a SOR.
     *
     * @param sorConfig SOR configuration.
     * @param cmd       A {@link SorHashRequest} instance.
     * @return A {@link SorHashResponse} instance containing information
     * about the hash results.
     * @throws HashExecutorException If an error occurred.
     */
    public SorHashResponse hash(SorConfigProperties sorConfig, SorHashRequest cmd) throws HashExecutorException {
        Integer sorId = registryService.getSorId(sorConfig.getSorName());
        if (sorId == null) {
            throw new HashExecutorException("Unable to find sorName " + sorConfig.getSorName() + " in the SOR table");
        }

        Timestamp lastTimeMarker = hashExecutorService.getLastTimeMarker(sorId);

        if (!sorConfig.isHashQueryTimestampSupported() || cmd.isFull() || lastTimeMarker == null) {
            // Full hash query, hash everything in the SOR
            try {
                return hashFullMode(sorConfig, sorId);
            } catch (IOException | SQLException | SearchExecutorException e) {
                throw new HashExecutorException(e);
            }
        } else {
            try {
                return hashLastChangedMode(sorConfig, sorId, lastTimeMarker);
            } catch (SearchExecutorException e) {
                throw new HashExecutorException(e);
            }
        }
    }

    private SorHashResponse hashFullMode(SorConfigProperties sorConfig, int sorId) throws IOException, HashExecutorException, SQLException, SearchExecutorException {
        log.info("For " + sorConfig.getSorName() + ", doing full hash");
        File csvFile = null;
        try {
            // For performance, leverage PostgreSQL's COPY feature.
            // Create a temp file to receive the hash values
            csvFile = File.createTempFile("sorhash", ".csv");
            csvFile.deleteOnExit();

            // Execute the query and write the hash values to the temp file
            Counter toCsvFileCounter = runFullQueryAndGenerateCsvFile(sorConfig, sorId, csvFile);
            if (toCsvFileCounter.getFailCount() > 0) {
                log.warn("For " + sorConfig.getSorName() + ", there were errors generating hash values for some entries.  Failure count=" + toCsvFileCounter.getFailCount());
            }

            // COPY the CSV file to the SORObjectChecksum table
            long toTableCount = hashExecutorService.copyFileToChecksumTable(sorId, csvFile);
            if (toTableCount != toCsvFileCounter.getSuccessCount()) {
                log.warn("For " + sorConfig.getSorName() + ", was expecting to write " + toCsvFileCounter.getSuccessCount() + " rows to table from CSV file but wrote " + toTableCount + " instead");
            }

            return new SorHashResponse(sorConfig.getSorName(), SorHashResponse.HashMode.FULL, toCsvFileCounter.getSuccessCount(), toCsvFileCounter.getFailCount());
        } finally {
            if (csvFile != null) {
                csvFile.delete();
            }
        }
    }

    private Counter runFullQueryAndGenerateCsvFile(SorConfigProperties sorConfig, int sorId, File csvFile) throws SearchExecutorException {
        OffsetDateTime hashTime = OffsetDateTime.now();
        var searchExecutor = getSearchExecutor();
        Counter counter = null;
        var context = searchExecutor.createContext(sorConfig);
        try {
            try (final PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
                counter = searchExecutor.searchHashFullMode(sorConfig, context, hashTime, (hashEntryContent) -> {
                    Timestamp timeMarker = hashEntryContent.getTimeMarker();
                    String sorObjKey = hashEntryContent.getSorObjKey();
                    long hash = hashEntryContent.getHash();
                    writer.println(sorId +
                            "|" + sorObjKey +
                            "|" + hash +
                            "|1" + // deprecated hashVersion
                            "|" + timeMarker +
                            "|" + hashEntryContent.getNumericMarker()
                    );
                });
            } catch (IOException e) {
                throw new SearchExecutorException(e);
            }
        } finally {
            try {
                context.close();
            } catch (Exception e) {
                log.warn("For " + sorConfig.getSorName() + ", there was a problem closing the context", e);
            }
        }
        return counter;
    }

    private SorHashResponse hashLastChangedMode(SorConfigProperties sorConfig, int sorId, Timestamp lastTimeMarker) throws SearchExecutorException {
        log.info("For " + sorConfig.getSorName() + ", doing hash using lastTimeMarker=" + lastTimeMarker);
        OffsetDateTime hashTime = OffsetDateTime.now();
        var searchExecutor = getSearchExecutor();
        Counter counter = null;
        var context = searchExecutor.createContext(sorConfig);
        try {
            counter = searchExecutor.searchHashLastChangedMode(sorConfig, context, hashTime, lastTimeMarker, (hashEntryContent) -> {
                Timestamp timeMarker = hashEntryContent.getTimeMarker();
                String sorObjKey = hashEntryContent.getSorObjKey();
                long hash = hashEntryContent.getHash();
                hashExecutorService.updateSORObjectChecksum(sorId, sorObjKey, hash, timeMarker, hashEntryContent.getNumericMarker());
            });
        } finally {
            try {
                context.close();
            } catch (Exception e) {
                log.error("For " + sorConfig.getSorName() + ", there was a problem closing the context", e);
            }
        }
        return new SorHashResponse(sorConfig.getSorName(), SorHashResponse.HashMode.LAST_CHANGED, counter.getSuccessCount(), counter.getFailCount());
    }
}
