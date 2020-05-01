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
package edu.berkeley.bidms.orm.collection;

import edu.berkeley.bidms.orm.collection.RebuildableSortedSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Extends {@link TreeSet} to implement {@link RebuildableSortedSet}, which
 * provides the ability to rebuild the sorted set.  It's useful to rebuild
 * the sorted set when values of set elements change such that it changes the
 * ordering of the set.
 */
public class RebuildableTreeSet<E> extends TreeSet<E> implements RebuildableSortedSet<E> {

    public RebuildableTreeSet() {
    }

    public RebuildableTreeSet(Comparator<? super E> comparator) {
        super(comparator);
    }

    public RebuildableTreeSet(Collection<? extends E> c) {
        super(c);
    }

    public RebuildableTreeSet(SortedSet<E> s) {
        super(s);
    }

    /**
     * Re-sort the underlying sorted set.  Intended to be called when the
     * ordering of the set may have changed due to element value changes.
     */
    @Override
    public void rebuild() {
        Collection<E> cloned = new ArrayList<>(this);
        clear();
        addAll(cloned);
    }
}
