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

import java.time.OffsetDateTime;

/**
 * Extracts content out of a data object from a SOR.
 *
 * @param <T> The type of object being parsed.
 * @param <R> The type of the resulting object that contains the extracted
 *            data.
 */
public interface EntryContentExtractor<T, R> {
    /**
     * Extracts query content out of a SOR data object and returns an object
     * that contains the extracted data.
     *
     * @param sorConfig A {@link SorConfigProperties} instance that contains
     *                  configuration for the SOR.
     * @param queryTime A {@link OffsetDateTime} instance that indicates when
     *                  the query was begun.
     * @param sorData   An instance of a SOR data object.
     * @return An instance containing the  extracted data.
     * @throws EntryContentExtractorException If there was an error
     *                                        extracting content of the
     *                                        entry.
     */
    R extractContent(SorConfigProperties sorConfig, OffsetDateTime queryTime, T sorData) throws EntryContentExtractorException;
}
