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
import java.util.LinkedList;
import java.util.List;

/**
 * Contains a collection of {@link HashDifference} values representing hash
 * value differences between the {@code SORObjectChecksum} table and the
 * {@code SORObject} table.
 */
public class HashDifferences {
    final private List<HashDifference> hashDifferences = new LinkedList<>();
    private Timestamp minimumHashTimeMarker;

    final private List<String> deletedSorObjectKeys = new LinkedList<>();

    /**
     * @return keys that have been removed from the SOR but not yet removed
     * from the {@code SORObject} table.
     */
    public List<String> getDeletedSorObjectKeys() {
        return deletedSorObjectKeys;
    }

    /**
     * @return A collection of {@link HashDifference} values.
     */
    public List<HashDifference> getHashDifferences() {
        return hashDifferences;
    }

    /**
     * If a SOR supports it, a timeMarker for an object is the "last
     * modified" time for the object.  The minimum (earliest) last modified
     * time of a hash difference collection is useful for querying in "last
     * changed mode". This value is updated as {@link #addHashDifference(String,
     * long, boolean, Timestamp)} is called.
     */
    public Timestamp getMinimumHashTimeMarker() {
        return minimumHashTimeMarker;
    }

    /**
     * Add a hash difference to the collection of hash differences.
     *
     * @param sorObjKey      The primary key of the object.
     * @param hash           The hash value in the {@code SORObjectChecksum}
     *                       table.
     * @param isNew          true if the object is in the SOR but not yet in
     *                       the {@code SORObject} table.
     * @param hashTimeMarker The timeMarker is the "last update" time for the
     *                       object, if supported within the SOR.  If not
     *                       supported in the SOR, this may be populated with
     *                       time of the hash query.
     */
    public void addHashDifference(final String sorObjKey, final long hash, final boolean isNew, Timestamp hashTimeMarker) {
        hashDifferences.add(new HashDifference(sorObjKey, hash, isNew, hashTimeMarker));
        if (minimumHashTimeMarker == null || hashTimeMarker.compareTo(minimumHashTimeMarker) < 0) {
            this.minimumHashTimeMarker = hashTimeMarker;
        }
    }
}
