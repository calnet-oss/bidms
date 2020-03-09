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
import java.util.Objects;

/**
 * A "hash difference" means there's a difference between what's in the
 * {@code SORObjectChecksum} table and what's in the {@code SORObject} table,
 * so the {@code SORObject} needs to be re-queried from the SOR and needs to
 * be updated in the Registry.
 * <p>
 * The {@code SORObjectChecksum} table is populated by step 1 "hash
 * queries".
 */
public class HashDifference {
    private final String sorObjKey;
    private final long hash;
    private final boolean isNew;
    private final Timestamp hashTimeMarker;

    public HashDifference(String sorObjKey, long hash, boolean isNew, Timestamp hashTimeMarker) {
        this.sorObjKey = sorObjKey;
        this.hash = hash;
        this.isNew = isNew;
        this.hashTimeMarker = hashTimeMarker;
    }

    /**
     * @return The sorObjKey is the primary key in the SOR for the SOR
     * object.
     */
    public String getSorObjKey() {
        return sorObjKey;
    }

    /**
     * @return The hash value in the {@code SORObjectChecksum} table.  i.e.,
     * this is the "new hash value".
     */
    public long getHash() {
        return hash;
    }

    /***
     * @return true if there is a row for the {@code sorObjKey} in the
     * {@code SORObjectChecksum} table but not in the {@code SORObject}
     * table, meaning this is a newly added primary key in the SOR.  The
     * {@code SORObject} row will need to be be created.
     */
    public boolean isNew() {
        return isNew;
    }

    /**
     * @return The timeMarker is the "last update" time for the object, if
     * supported within the SOR.  If not supported in the SOR, this may be
     * populated with time of the hash query.
     */
    public Timestamp getHashTimeMarker() {
        return hashTimeMarker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashDifference)) return false;
        HashDifference that = (HashDifference) o;
        return hash == that.hash &&
                isNew == that.isNew &&
                sorObjKey.equals(that.sorObjKey) &&
                Objects.equals(hashTimeMarker, that.hashTimeMarker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sorObjKey, hash, isNew, hashTimeMarker);
    }
}
