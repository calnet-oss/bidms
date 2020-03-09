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

import java.sql.Timestamp;
import java.time.OffsetDateTime;

/**
 * Contains hash and time marker values for entries within a SOR.
 *
 * @param <N> The type of native content supported by the {@link
 *            SearchExecutor} implementation.
 */
public class HashEntryContent<N> extends QueryEntryContent<N> {
    private final String fullIdentifier;
    private final Timestamp timeMarker;
    private final long numericMarker;
    private final long hash;

    /**
     * @param sorName SOR name.
     * @param fullIdentifier Full identifier of the SOR object.
     * @param sorObjKey Primary key of the SOR object.
     * @param hashTime When the hash query was started.
     * @param nativeContent Native hash content.
     * @param timeMarker If supported, last modified time of the SOR object.
     * @param numericMarker Numeric marker.  Often the timeMarker converted as epoch time.
     * @param hash The hash value of the SOR object.
     */
    public HashEntryContent(
            String sorName,
            String fullIdentifier,
            String sorObjKey,
            OffsetDateTime hashTime,
            N nativeContent,
            Timestamp timeMarker,
            long numericMarker,
            long hash
    ) {
        super(sorName, sorObjKey, hashTime, nativeContent);
        this.fullIdentifier = fullIdentifier;
        this.timeMarker = timeMarker;
        this.numericMarker = numericMarker;
        this.hash = hash;
    }

    /**
     * @param sorName SOR name.
     * @param fullIdentifier Full identifier of the SOR object.
     * @param sorObjKey Primary key of the SOR object.
     * @param hashTime When the hash query was started.
     * @param timeMarker If supported, last modified time of the SOR object.
     * @param numericMarker Numeric marker.  Often the timeMarker converted as epoch time.
     * @param hash The hash value of the SOR object.
     */
    public HashEntryContent(
            String sorName,
            String fullIdentifier,
            String sorObjKey,
            OffsetDateTime hashTime,
            Timestamp timeMarker,
            long numericMarker,
            long hash
    ) {
        this(sorName, fullIdentifier, sorObjKey, hashTime, null, timeMarker, numericMarker, hash);
    }

    /**
     * @return A full distinguishing (unique) identifier for a SOR object. In
     * many cases this will be the same as the {@code sorObjKey} but in some
     * cases there may be a fuller form of the primary key.  For example, in
     * LDAP, we may use {@code uid} as the sorObjKey but the full identifier
     * is the DN.
     */
    @Override
    public String getFullIdentifier() {
        return fullIdentifier;
    }

    /**
     * @return A timeMarker is the the "last modified" time of the entry, if
     * the SOR supports it.
     */
    public Timestamp getTimeMarker() {
        return timeMarker;
    }

    /**
     * @return A numericMarker.  Often the long epoch of the timeMarker
     * unless the SOR supports a different numeric marker (not common).
     */
    public long getNumericMarker() {
        return numericMarker;
    }

    /**
     * @return The hash value of the object.
     */
    public long getHash() {
        return hash;
    }
}
