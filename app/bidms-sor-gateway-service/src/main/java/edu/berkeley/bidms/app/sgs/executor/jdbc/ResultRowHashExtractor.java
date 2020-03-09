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
package edu.berkeley.bidms.app.sgs.executor.jdbc;

import edu.berkeley.bidms.app.sgs.config.properties.SorConfigProperties;
import edu.berkeley.bidms.app.sgs.executor.EntryContentExtractor;
import edu.berkeley.bidms.app.sgs.executor.EntryContentExtractorException;
import edu.berkeley.bidms.app.sgs.executor.ExecutorUtil;
import edu.berkeley.bidms.app.sgs.executor.HashEntryContent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;

/**
 * Extracts a hash value from a JDBC {@link ResultSet} row.
 */
public class ResultRowHashExtractor implements EntryContentExtractor<ResultSet, HashEntryContent<Void>> {

    private int sorObjKeyColumnIdx;
    private int hashColumnIdx;
    private Integer timeMarkerColumnIdx;

    public ResultRowHashExtractor(int sorObjKeyColumnIdx, int hashColumnIdx, Integer timeMarkerColumnIdx) {
        this.sorObjKeyColumnIdx = sorObjKeyColumnIdx;
        this.hashColumnIdx = hashColumnIdx;
        this.timeMarkerColumnIdx = timeMarkerColumnIdx;
    }

    /**
     * Extracts a hash value (and, if available, a timeMarker) from a current
     * JDBC {@link ResultSet} row.  The hash value is produced on the
     * database side and is a column value within the current {@link
     * ResultSet} row.
     *
     * @param sorConfig SOR configuration.
     * @param hashTime  The time the hash query was started.
     * @param rs        The current {@link ResultSet} row that contains a
     *                  hash value.
     * @return A {@link HashEntryContent} instance that contains the hash
     * information for the current row.
     * @throws EntryContentExtractorException If an error occurred.
     */
    @Override
    public HashEntryContent<Void> extractContent(SorConfigProperties sorConfig, OffsetDateTime hashTime, ResultSet rs) throws EntryContentExtractorException {
        try {
            String sorObjKey = rs.getString(sorObjKeyColumnIdx);
            // when timeMarker is supported, timeMarker is required, but use current time otherwise
            Timestamp timeMarker = sorConfig.isHashQueryTimestampSupported() ? rs.getTimestamp(timeMarkerColumnIdx) : Timestamp.from(hashTime.toInstant());
            long numericMarker = ExecutorUtil.convertTimestampToMicroseconds(timeMarker);
            return new HashEntryContent<>(sorConfig.getSorName(), sorObjKey, sorObjKey, hashTime, timeMarker, numericMarker, rs.getLong(hashColumnIdx));
        } catch (SQLException e) {
            throw new EntryContentExtractorException(e);
        }
    }
}
