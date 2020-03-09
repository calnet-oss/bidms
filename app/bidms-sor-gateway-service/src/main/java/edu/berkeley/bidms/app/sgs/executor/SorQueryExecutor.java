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
import edu.berkeley.bidms.app.sgs.model.request.SorQueryRequest;
import edu.berkeley.bidms.app.sgs.model.response.SorQueryResponse;
import edu.berkeley.bidms.app.sgs.service.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Perform queries on a SOR.
 *
 * @param <C> The context type which is typically something like a connection
 *            to the SOR.
 * @param <Q> A type that implements {@link QueryEntryContent}.
 */
public abstract class SorQueryExecutor<C extends AutoCloseable, Q extends QueryEntryContent<?>> {
    private final Logger log = LoggerFactory.getLogger(SorQueryExecutor.class);

    private RegistryService registryService;
    private QueryExecutorService queryExecutorService;

    public SorQueryExecutor(RegistryService registryService, QueryExecutorService queryExecutorService) {
        this.registryService = registryService;
        this.queryExecutorService = queryExecutorService;
    }

    /**
     * @return A {@link SearchExecutor} implementation.
     */
    protected abstract SearchExecutor<C, ?, Q> getSearchExecutor();

    /**
     * Perform a query on a SOR.
     *
     * @param sorConfig SOR configuration.
     * @param cmd       A {@link SorQueryRequest} instance.
     * @return A {@link SorQueryResponse} instance containing information
     * about the query results.
     * @throws QueryExecutorException If an error occurred.
     */
    public SorQueryResponse query(SorConfigProperties sorConfig, SorQueryRequest cmd) throws QueryExecutorException {
        Integer sorId = registryService.getSorId(sorConfig.getSorName());
        if (sorId == null) {
            throw new QueryExecutorException("Unable to find sorName " + sorConfig.getSorName() + " in the SOR table");
        }

        Integer sorObjectCount = queryExecutorService.getSorObjectCount(sorId);
        if (sorObjectCount == null || sorObjectCount == 0) {
            log.info("For " + sorConfig.getSorName() + ", forcing full query mode because the SORObject table is empty for this SOR");
            cmd.setFull(true);
        }

        try {
            if (cmd.isFull()) {
                return queryFullMode(sorConfig, sorId);
            } else {
                if (!sorConfig.isQueryTimestampSupported()) {
                    return queryIndividualMode(sorConfig, sorId);
                } else {
                    SorQueryResponse lastChangedResult = queryLastChangedMode(sorConfig, sorId);
                    SorQueryResponse individualResult = queryIndividualMode(sorConfig, sorId);
                    return new SorQueryResponse(
                            cmd.getSorName(),
                            SorQueryResponse.QueryMode.LAST_CHANGED,
                            lastChangedResult.getSuccessfulQueryCount() + individualResult.getSuccessfulQueryCount(),
                            lastChangedResult.getFailedQueryCount() + individualResult.getFailedQueryCount(),
                            lastChangedResult.getDeletedCount() + individualResult.getDeletedCount()
                    );
                }
            }
        } catch (SearchExecutorException e) {
            throw new QueryExecutorException(e);
        }
    }

    private SorQueryResponse queryFullMode(SorConfigProperties sorConfig, int sorId) throws QueryExecutorException, SearchExecutorException {
        /* Query for existing local sor object keys because in full mode, we
         * need to know to create, update, or delete keys locally.  We split
         * the local keys into two categories: normal and soft-deleted.
         * Soft-deleted keys are a special case where they will be undeleted
         * as an update if they newly exist in the SOR again.  If they don't
         * exist in the SOR, then it's a no-op (they stay soft-deleted
         * locally).  */
        log.info("For " + sorConfig.getSorName() + ", in full query mode, querying for existing SORObject keys");
        QueryExecutorService.ExistingSorObjects existingSorObjects = queryExecutorService.queryForExistingSorObjectKeysAndHashes(sorId);
        log.info("For " + sorConfig.getSorName() + ", got " + existingSorObjects.getExistingSoftDeletedSorObjectKeys().size() + " soft deleted keys and " + existingSorObjects.getExistingSorObjectKeys().size() + " existing keys" + " and " + existingSorObjects.getExistingSorObjectHashes().size() + " hashes");

        List<String> sorObjectKeysFromSor = new LinkedList<>();

        OffsetDateTime queryTime = OffsetDateTime.now();
        var searchExecutor = getSearchExecutor();
        Counter counter = null;
        var context = searchExecutor.createContext(sorConfig);
        try {
            counter = searchExecutor.searchQueryFullMode(sorConfig, context, queryTime, (queryEntryContent) -> {
                sorObjectKeysFromSor.add(queryEntryContent.getSorObjKey());
                boolean isExisting = existingSorObjects.getExistingSorObjectKeys().contains(queryEntryContent.getSorObjKey()) || existingSorObjects.getExistingSoftDeletedSorObjectKeys().contains(queryEntryContent.getSorObjKey());
                Long existingHash = existingSorObjects.getExistingSorObjectHashes().get(queryEntryContent.getSorObjKey());
                try {
                    if (!isExisting) {
                        queryExecutorService.createSorObject(sorConfig, sorId, queryEntryContent.getSorObjKey(), existingHash, queryEntryContent);
                    } else {
                        queryExecutorService.updateSorObject(sorConfig, sorId, queryEntryContent.getSorObjKey(), existingHash, queryEntryContent);
                    }
                } catch (QueryExecutorException e) {
                    log.error("Could not persist SORObject for " + sorConfig.getSorName() + " " + queryEntryContent.getFullIdentifier(), e);
                }
            });
        } finally {
            try {
                context.close();
            } catch (Exception e) {
                log.error("For " + sorConfig.getSorName() + ", there was a problem closing the context", e);
            }
        }

        SorQueryResponse queryResult = new SorQueryResponse(sorConfig.getSorName(), SorQueryResponse.QueryMode.FULL);
        queryResult.setSuccessfulQueryCount(counter.getSuccessCount());
        queryResult.setFailedQueryCount(counter.getFailCount());

        // sorObjectKeysToDelete == existingSorObjectKeys - sorObjectKeysFromSor
        // The existingSorObjectKeys set does not contain the keys already soft-deleted.
        List<String> sorObjectKeysToDelete = new ArrayList<>(existingSorObjects.getExistingSorObjectKeys());
        log.info("For " + sorConfig.getSorName() + ", there are " + existingSorObjects.getExistingSorObjectKeys().size() + " existing keys and " + sorObjectKeysFromSor.size() + " keys queried from SOR");
        sorObjectKeysToDelete.removeAll(sorObjectKeysFromSor);

        // delete keys that are in SORObject table (and not soft-deleted already) but not in the SOR query result set
        log.info("For " + sorConfig.getSorName() + ", there are " + sorObjectKeysToDelete.size() + " old keys to delete");
        int deleteCounter = 0;
        for (String sorObjKey : sorObjectKeysToDelete) {
            if (queryExecutorService.deleteSorObject(sorConfig, sorId, sorObjKey)) {
                deleteCounter++;
            }
        }
        queryResult.setDeletedCount(deleteCounter);
        return queryResult;
    }

    private SorQueryResponse queryIndividualMode(SorConfigProperties sorConfig, int sorId) throws QueryExecutorException, SearchExecutorException {
        HashDifferences hashDifferences = queryExecutorService.getHashDifferences(sorId);
        log.info("For " + sorConfig.getSorName() + ", in individual mode, got " + hashDifferences.getHashDifferences().size() + " new or changed and " + hashDifferences.getDeletedSorObjectKeys().size() + " deleted");
        var searchExecutor = getSearchExecutor();
        int successfulQueryCount = 0;
        int failedQueryCount = 0;
        int deleteCount = 0;
        var context = searchExecutor.createContext(sorConfig);
        try {
            for (HashDifference hashDifference : hashDifferences.getHashDifferences()) {
                OffsetDateTime queryTime = OffsetDateTime.now();
                final Map<String, Boolean> foundSorObjectKeys = new HashMap<>();
                Counter counter = searchExecutor.searchQueryIndividualMode(sorConfig, context, queryTime, hashDifference.getSorObjKey(), (queryEntryContent) -> {
                    if (!queryEntryContent.getSorObjKey().equals(hashDifference.getSorObjKey())) {
                        log.error("There is a query bug: For " + sorConfig.getSorName() + " " + queryEntryContent.getFullIdentifier() + ", expected to get sorObjKey=" + hashDifference.getSorObjKey() + " but got " + queryEntryContent.getSorObjKey() + " instead");
                        return;
                    }

                    foundSorObjectKeys.put(queryEntryContent.getSorObjKey(), Boolean.TRUE);

                    try {
                        if (hashDifference.isNew()) {
                            queryExecutorService.createSorObject(sorConfig, sorId, queryEntryContent.getSorObjKey(), hashDifference.getHash(), queryEntryContent);
                        } else {
                            queryExecutorService.updateSorObject(sorConfig, sorId, queryEntryContent.getSorObjKey(), hashDifference.getHash(), queryEntryContent);
                        }
                    } catch (QueryExecutorException e) {
                        log.error("Could not persist SORObject for " + sorConfig.getSorName() + " " + queryEntryContent.getFullIdentifier(), e);
                    }
                });
                if (counter.getSuccessCount() == 1) {
                    successfulQueryCount++;
                } else {
                    failedQueryCount++;
                }

                if (!foundSorObjectKeys.containsKey(hashDifference.getSorObjKey())) {
                    // not found, so delete both the SORObject and the SORObjectChecksum rows for the sorObjKey.
                    log.info("For " + sorConfig.getSorName() + ", " + hashDifference.getSorObjKey() + " not found in SOR when querying individually so deleting SORObject");
                    if (queryExecutorService.deleteSorObject(sorConfig, sorId, hashDifference.getSorObjKey())) {
                        deleteCount++;
                    }
                    queryExecutorService.deleteSorObjectChecksum(sorId, hashDifference.getSorObjKey());
                }
            }
        } finally {
            try {
                context.close();
            } catch (Exception e) {
                log.error("For " + sorConfig.getSorName() + ", there was a problem closing the context", e);
            }
        }

        // Deletes
        for (String sorObjKeyToDelete : hashDifferences.getDeletedSorObjectKeys()) {
            if (queryExecutorService.deleteSorObject(sorConfig, sorId, sorObjKeyToDelete)) {
                deleteCount++;
            }
        }

        return new SorQueryResponse(sorConfig.getSorName(), SorQueryResponse.QueryMode.INDIVIDUAL, successfulQueryCount, failedQueryCount, deleteCount);
    }

    private Map<String, HashDifference> mapHashDifferences(List<HashDifference> hashDifferences) {
        return hashDifferences.stream().collect(Collectors.toMap(HashDifference::getSorObjKey, Function.identity()));
    }

    private SorQueryResponse queryLastChangedMode(SorConfigProperties sorConfig, int sorId) throws SearchExecutorException {
        HashDifferences hashDifferences = queryExecutorService.getHashDifferences(sorId);
        log.info("For " + sorConfig.getSorName() + ", in last changed mode, got " + hashDifferences.getHashDifferences().size() + " new or changed with a minimum lastTimeMarker of " + hashDifferences.getMinimumHashTimeMarker());
        SorQueryResponse queryResponse = new SorQueryResponse(sorConfig.getSorName(), SorQueryResponse.QueryMode.LAST_CHANGED);
        // If there are no hash differences, there's nothing to do.
        if (hashDifferences.getHashDifferences().size() == 0) {
            return queryResponse;
        }
        final Map<String, HashDifference> hashDifferencesMap = mapHashDifferences(hashDifferences.getHashDifferences());
        Timestamp lastTimeMarker = hashDifferences.getMinimumHashTimeMarker();
        // release hashDifferences reference to make it eligible for gc
        hashDifferences = null;

        OffsetDateTime queryTime = OffsetDateTime.now();
        var searchExecutor = getSearchExecutor();
        Counter counter = null;
        var context = searchExecutor.createContext(sorConfig);
        try {
            counter = searchExecutor.searchQueryLastChangedMode(sorConfig, context, queryTime, lastTimeMarker, (queryEntryContent) -> {
                HashDifference hashDifference = hashDifferencesMap.get(queryEntryContent.getSorObjKey());
                if (hashDifference == null) {
                    log.info("For " + sorConfig.getSorName() + ", " + queryEntryContent.getSorObjKey() + " did not appear in the hash difference list.  Ignoring.");
                    return;
                }

                try {
                    if (hashDifference.isNew()) {
                        queryExecutorService.createSorObject(sorConfig, sorId, queryEntryContent.getSorObjKey(), hashDifference.getHash(), queryEntryContent);
                    } else {
                        queryExecutorService.updateSorObject(sorConfig, sorId, queryEntryContent.getSorObjKey(), hashDifference.getHash(), queryEntryContent);
                    }
                } catch (QueryExecutorException e) {
                    log.error("Could not persist SORObject for " + sorConfig.getSorName() + " " + queryEntryContent.getFullIdentifier(), e);
                }
            });
            queryResponse.setSuccessfulQueryCount(counter.getSuccessCount());
            queryResponse.setFailedQueryCount(counter.getFailCount());
        } finally {
            try {
                context.close();
            } catch (Exception e) {
                log.error("For " + sorConfig.getSorName() + ", there was a problem closing the context", e);
            }
        }
        return queryResponse;
    }
}
