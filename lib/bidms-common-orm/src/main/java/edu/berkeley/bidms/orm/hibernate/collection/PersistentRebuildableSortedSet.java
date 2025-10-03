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
package edu.berkeley.bidms.orm.hibernate.collection;

import edu.berkeley.bidms.orm.collection.RebuildableSortedSet;
import org.hibernate.collection.spi.PersistentSortedSet;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.util.Comparator;
import java.util.SortedSet;

/**
 * Extends Hibernate's {@link PersistentSortedSet} by adding an
 * implementation of {@link RebuildableSortedSet} which provides the ability
 * to rebuild the underlying sorted set.  It's useful to rebuild the sorted
 * set when values of set elements change such that it changes the ordering
 * of the set.
 */
@SuppressWarnings("rawtypes")
public class PersistentRebuildableSortedSet<E> extends PersistentSortedSet<E> implements RebuildableSortedSet<E> {
    public PersistentRebuildableSortedSet() {
        super();
    }

    public PersistentRebuildableSortedSet(SharedSessionContractImplementor session, Comparator<E> comparator) {
        super(session, comparator);
    }

    public PersistentRebuildableSortedSet(SharedSessionContractImplementor session, SortedSet<E> set) {
        super(session, set);
    }

    /**
     * Re-sort the underlying sorted set.  Intended to be called when the
     * ordering of the set may have changed due to element value changes.
     */
    public void rebuild() {
        if (set != null) {
            if (!(set instanceof RebuildableSortedSet)) {
                throw new RuntimeException("The underlying set does not implement the RebuildableSortedSet interface.  The underlying set type is " + set.getClass().getName());
            } else {
                ((RebuildableSortedSet) set).rebuild();
            }
        }
    }
}
