package edu.berkeley.hibernate.usertype;

import org.hibernate.HibernateException;
import org.hibernate.collection.internal.PersistentSortedSet;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class RegistrySortedSetType implements UserCollectionType {
    @Override
    public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister) throws HibernateException {
        return new RegistryPersistentSortedSet(session);
    }

    @Override
    public PersistentCollection wrap(SessionImplementor session, Object collection) {
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

    @Override
    @SuppressWarnings("unchecked")
    public Object replaceElements(Object original, Object target, CollectionPersister persister, Object owner, Map copyCache, SessionImplementor session) throws HibernateException {
        SortedSet result = (SortedSet) target;
        result.clear();
        result.addAll((SortedSet) target);
        return result;
    }

    @Override
    public Object instantiate(int anticipatedSize) {
        return newSet(Object.class);
    }

    public static <T> SortedSet<T> newSet(Class<T> clazz) {
        // We use ConcurrentSkipListSet instead of TreeSet because TreeSets
        // seem to throw an exception when replaceElements() gets called.
        return new ConcurrentSkipListSet<>();
    }

    static class RegistryPersistentSortedSet extends PersistentSortedSet {
        public RegistryPersistentSortedSet() {
            super();
        }

        RegistryPersistentSortedSet(SessionImplementor session) {
            super(session);
        }

        RegistryPersistentSortedSet(SessionImplementor session, SortedSet set) {
            super(session, set);
        }
    }
}
