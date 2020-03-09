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

import java.sql.Timestamp;
import java.time.OffsetDateTime;

/**
 * An interface for the implementations that perform hash queries and object
 * data retrieval queries on SORs.
 *
 * @param <C> The context type which is typically something like a connection
 *            to the SOR.
 * @param <H> A type that implements {@link HashEntryContent}.
 * @param <Q> A type that implements {@link QueryEntryContent}.
 */
public interface SearchExecutor<C extends AutoCloseable, H extends HashEntryContent<?>, Q extends QueryEntryContent<?>> {

    /**
     * @param sorConfig SOR configuration.
     * @return The context type which is typically something like a
     * connection to the SOR.
     * @throws SearchExecutorException If there was a problem creating the
     *                                 context.
     */
    C createContext(SorConfigProperties sorConfig) throws SearchExecutorException;

    /**
     * Perform a "full mode" query which queries for all entries matching a
     * search criteria.
     *
     * @param sorConfig       SOR configuration.
     * @param ctx             The context type which is typically something
     *                        like a connection to the SOR.
     * @param queryTime       The time the query started.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on each entry as the
     *                        search is performed and each entry in the
     *                        result is iterated.
     * @return An instance of {@link Counter} that indicates quantity of
     * successful entries processed and quantity of failed entry attempts.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any entries from being iterated.
     */
    Counter searchQueryFullMode(SorConfigProperties sorConfig, C ctx, OffsetDateTime queryTime, QueryEntryCallback<Q> callbackHandler) throws SearchExecutorException;

    /**
     * Perform an "individual mode" query which queries for a single key.
     *
     * @param sorConfig       SOR configuration.
     * @param ctx             The context type which is typically something
     *                        like a connection to the SOR.
     * @param queryTime       The time the query started.
     * @param sorObjKey       The primary key to search for.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on a found entry.
     * @return An instance of {@link Counter}.  Since we are only searching
     * for one key, either the success count will be 1 or the failure count
     * will be 1.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any entries from being iterated.
     */
    Counter searchQueryIndividualMode(SorConfigProperties sorConfig, C ctx, OffsetDateTime queryTime, String sorObjKey, QueryEntryCallback<Q> callbackHandler) throws SearchExecutorException;

    /**
     * Perform a "last changed mode" query which queries for entries that
     * have changed since a certain time.
     *
     * @param sorConfig       SOR configuration.
     * @param ctx             The context type which is typically something
     *                        like a connection to the SOR.
     * @param queryTime       The time the query started.
     * @param lastQueryTime   Query for changed entries since this
     *                        timestamp.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on each entry as the
     *                        search is performed and each entry in the
     *                        result is iterated.
     * @return An instance of {@link Counter} that indicates quantity of
     * successful entries processed and quantity of failed entry attempts.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any entries from being iterated.
     */
    Counter searchQueryLastChangedMode(SorConfigProperties sorConfig, C ctx, OffsetDateTime queryTime, Timestamp lastQueryTime, QueryEntryCallback<Q> callbackHandler) throws SearchExecutorException;

    /**
     * Perform a "full mode" hash query which queries for all entries
     * matching a search criteria.
     *
     * @param sorConfig       SOR configuration.
     * @param ctx             The context type which is typically something
     *                        like a connection to the SOR.
     * @param hashTime        The time the hash query started.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on each entry as the
     *                        search is performed and each entry in the
     *                        result is iterated.
     * @return An instance of {@link Counter} that indicates quantity of
     * successful entries processed and quantity of failed entry attempts.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any entries from being iterated.
     */
    Counter searchHashFullMode(SorConfigProperties sorConfig, C ctx, OffsetDateTime hashTime, QueryEntryCallback<H> callbackHandler) throws SearchExecutorException;

    /**
     * Perform an "individual mode" hash query which queries for a single
     * key.
     *
     * @param sorConfig       SOR configuration.
     * @param ctx             The context type which is typically something
     *                        like a connection to the SOR.
     * @param hashTime        The time the hash query started.
     * @param sorObjKey       The primary key to search for.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on a found entry.
     * @return An instance of {@link Counter}.  Since we are only searching
     * for one key, either the success count will be 1 or the failure count
     * will be 1.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any entries from being iterated.
     */
    Counter searchHashIndividualMode(SorConfigProperties sorConfig, C ctx, OffsetDateTime hashTime, String sorObjKey, QueryEntryCallback<H> callbackHandler) throws SearchExecutorException;

    /**
     * Perform a "last changed mode" hash query which queries for entries
     * that have changed since a certain time.
     *
     * @param sorConfig       SOR configuration.
     * @param ctx             The context type which is typically something
     *                        like a connection to the SOR.
     * @param hashTime        The time the hash query started.
     * @param lastQueryTime   Query for changed entries since this
     *                        timestamp.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on each entry as the
     *                        search is performed and each entry in the
     *                        result is iterated.
     * @return An instance of {@link Counter} that indicates quantity of
     * successful entries processed and quantity of failed entry attempts.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any entries from being iterated.
     */
    Counter searchHashLastChangedMode(SorConfigProperties sorConfig, C ctx, OffsetDateTime hashTime, Timestamp lastQueryTime, QueryEntryCallback<H> callbackHandler) throws SearchExecutorException;
}
