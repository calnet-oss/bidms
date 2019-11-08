/*
 * Copyright (c) 2019, Regents of the University of California and
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
package edu.berkeley.bidms.registryModel.hibernate.usertype;

import org.hibernate.HibernateException;
import org.hibernate.collection.internal.PersistentSortedSet;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Implements a Hibernate {@link UserCollectionType} that utilizes {@link
 * PersistentSortedSet} to wrap {@link ConcurrentSkipListSet} as its
 * collection.
 */
public class RegistrySortedSetType implements UserCollectionType {
    @Override
    public PersistentCollection instantiate(SharedSessionContractImplementor session, CollectionPersister persister) throws HibernateException {
        return new RegistryPersistentSortedSet(session);
    }

    @Override
    public PersistentCollection wrap(SharedSessionContractImplementor session, Object collection) {
        return new RegistryPersistentSortedSet(session, (SortedSet) collection);
    }

    @Override
    public Iterator getElementsIterator(Object collection) {
        return ((SortedSet) collection).iterator();
    }

    @Override
    public boolean contains(Object collection, Object entity) {
        return ((SortedSet) collection).contains(entity);
    }

    @Override
    public Object indexOf(Object collection, Object entity) {
        throw new UnsupportedOperationException("indexOf not supported for a set");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object replaceElements(Object original, Object target, CollectionPersister persister, Object owner, Map copyCache, SharedSessionContractImplementor session) throws HibernateException {
        SortedSet result = (SortedSet) target;
        result.clear();
        result.addAll((SortedSet) target);
        return result;
    }

    @Override
    public Object instantiate(int anticipatedSize) {
        return newSet(Comparable.class);
    }

    public static <T extends Comparable> SortedSet<T> newSet(Class<T> clazz) {
        // We use ConcurrentSkipListSet instead of TreeSet because TreeSets
        // seem to throw an exception when replaceElements() gets called.
        return new ConcurrentSkipListSet<>();
    }

    static class RegistryPersistentSortedSet extends PersistentSortedSet {
        public RegistryPersistentSortedSet() {
            super();
        }

        RegistryPersistentSortedSet(SharedSessionContractImplementor session) {
            super(session);
        }

        RegistryPersistentSortedSet(SharedSessionContractImplementor session, SortedSet set) {
            super(session, set);
        }
    }
}
