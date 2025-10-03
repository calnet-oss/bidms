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
package edu.berkeley.bidms.orm.hibernate.usertype;

import edu.berkeley.bidms.orm.collection.RebuildableTreeSet;
import edu.berkeley.bidms.orm.hibernate.collection.PersistentRebuildableSortedSet;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.collection.spi.PersistentSortedSet;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.CollectionClassification;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.SortedSetType;
import org.hibernate.usertype.UserCollectionType;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

/**
 * Implements a Hibernate {@link UserCollectionType} that utilizes {@link
 * PersistentSortedSet} to wrap {@link RebuildableTreeSet} as its underlying
 * collection.
 */
public class RebuildableSortedSetType extends SortedSetType implements UserCollectionType {

    @SuppressWarnings("rawtypes")
    private final Comparator comparator;

    public RebuildableSortedSetType(String role, String propertyRef, Comparator comparator) {
        super(role, propertyRef, comparator);
        this.comparator = comparator;
    }

    @Override
    public CollectionClassification getClassification() {
        return getCollectionClassification();
    }

    @Override
    public Class<?> getCollectionClass() {
        return getReturnedClass();
    }

    @Override
    public PersistentCollection instantiate(SharedSessionContractImplementor session, CollectionPersister persister) {
        return new PersistentRebuildableSortedSet(session, comparator);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public PersistentCollection wrap(SharedSessionContractImplementor session, Object collection) {
        return new PersistentRebuildableSortedSet(session, (SortedSet) collection);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object instantiate(int anticipatedSize) {
        return new RebuildableTreeSet(comparator);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Iterator getElementsIterator(Object collection) {
        // the set is cloned to avoid ConcurrentModificationExceptions
        return new RebuildableTreeSet((Collection) collection).iterator();
    }

    @Override
    public boolean contains(Object collection, Object entity) {
        return super.contains(collection, entity, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object replaceElements(
            Object original,
            Object target,
            CollectionPersister persister,
            Object owner,
            Map copyCache,
            SharedSessionContractImplementor session
    ) throws HibernateException {
        return super.replaceElements(original, target, owner, copyCache, session);
    }
}
