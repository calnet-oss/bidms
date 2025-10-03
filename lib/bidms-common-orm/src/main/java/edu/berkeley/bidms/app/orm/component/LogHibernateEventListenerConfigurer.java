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
package edu.berkeley.bidms.app.orm.component;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.AutoFlushEvent;
import org.hibernate.event.spi.AutoFlushEventListener;
import org.hibernate.event.spi.ClearEvent;
import org.hibernate.event.spi.ClearEventListener;
import org.hibernate.event.spi.DeleteContext;
import org.hibernate.event.spi.DeleteEvent;
import org.hibernate.event.spi.DeleteEventListener;
import org.hibernate.event.spi.DirtyCheckEvent;
import org.hibernate.event.spi.DirtyCheckEventListener;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.EvictEvent;
import org.hibernate.event.spi.EvictEventListener;
import org.hibernate.event.spi.FlushEntityEvent;
import org.hibernate.event.spi.FlushEntityEventListener;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.spi.FlushEventListener;
import org.hibernate.event.spi.InitializeCollectionEvent;
import org.hibernate.event.spi.InitializeCollectionEventListener;
import org.hibernate.event.spi.LoadEvent;
import org.hibernate.event.spi.LoadEventListener;
import org.hibernate.event.spi.LockEvent;
import org.hibernate.event.spi.LockEventListener;
import org.hibernate.event.spi.MergeContext;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.event.spi.PersistContext;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.event.spi.PersistEventListener;
import org.hibernate.event.spi.PostCollectionRecreateEvent;
import org.hibernate.event.spi.PostCollectionRecreateEventListener;
import org.hibernate.event.spi.PostCollectionRemoveEvent;
import org.hibernate.event.spi.PostCollectionRemoveEventListener;
import org.hibernate.event.spi.PostCollectionUpdateEvent;
import org.hibernate.event.spi.PostCollectionUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreCollectionRecreateEvent;
import org.hibernate.event.spi.PreCollectionRecreateEventListener;
import org.hibernate.event.spi.PreCollectionRemoveEvent;
import org.hibernate.event.spi.PreCollectionRemoveEventListener;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.event.spi.RefreshContext;
import org.hibernate.event.spi.RefreshEvent;
import org.hibernate.event.spi.RefreshEventListener;
import org.hibernate.event.spi.ReplicateEvent;
import org.hibernate.event.spi.ReplicateEventListener;
import org.hibernate.event.spi.ResolveNaturalIdEvent;
import org.hibernate.event.spi.ResolveNaturalIdEventListener;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * For debugging purposes, logs the order of all the Hibernate events.
 */
public class LogHibernateEventListenerConfigurer {

    private final Logger log = LoggerFactory.getLogger(LogHibernateEventListenerConfigurer.class);

    private EntityManagerFactory entityManagerFactory;

    public LogHibernateEventListenerConfigurer(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @PostConstruct
    public void init() {
        init(entityManagerFactory.unwrap(SessionFactoryImplementor.class));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void init(SessionFactoryImplementor sessionFactory) {
        // https://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#events-events
        EventListenerRegistry eventListenerRegistry = sessionFactory
                .getServiceRegistry()
                .getService(EventListenerRegistry.class);

        Map<Class, List<EventType>> eventTypesForListenerInterfaces = getEventTypesForListenerInterfaces();
        for (Class listenerInterface : eventTypesForListenerInterfaces.keySet()) {
            log.debug(" listenerInterface=" + listenerInterface);
        }

        CompositeEventListener compositeEventListener = new CompositeEventListener();
        for (EventType eventType : EventType.values()) {
            eventListenerRegistry.prependListeners(eventType, compositeEventListener);
        }
    }

    @SuppressWarnings("rawtypes")
    private static Map<Class, List<EventType>> getEventTypesForListenerInterfaces() {
        return EventType.values().stream().collect(Collectors.groupingBy(EventType::baseListenerInterface));
    }

    public interface CompositeEventListenerInterface extends AutoFlushEventListener,
            ClearEventListener,
            DeleteEventListener,
            DirtyCheckEventListener,
            EvictEventListener,
            FlushEntityEventListener,
            FlushEventListener,
            InitializeCollectionEventListener,
            LoadEventListener,
            LockEventListener,
            MergeEventListener,
            PersistEventListener,
            PostCollectionRecreateEventListener,
            PostCollectionRemoveEventListener,
            PostCollectionUpdateEventListener,
            PostDeleteEventListener,
            PostInsertEventListener,
            PostLoadEventListener,
            PostUpdateEventListener,
            PreCollectionRecreateEventListener,
            PreCollectionRemoveEventListener,
            PreCollectionUpdateEventListener,
            PreDeleteEventListener,
            PreInsertEventListener,
            PreLoadEventListener,
            PreUpdateEventListener,
            RefreshEventListener,
            ReplicateEventListener,
            ResolveNaturalIdEventListener,
            SaveOrUpdateEventListener {
    }

    public static class CompositeEventListener implements CompositeEventListenerInterface {

        private final Logger log = LoggerFactory.getLogger(CompositeEventListener.class);

        @Override
        public void onAutoFlush(AutoFlushEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onClear(ClearEvent event) {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onDelete(DeleteEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onDelete(DeleteEvent event, DeleteContext transientEntities) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onDirtyCheck(DirtyCheckEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onEvict(EvictEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName() + " on " + event.getEntity().getClass().getName() + ", dirtyCheckPossible=" + event.isDirtyCheckPossible() + ", dirtyCheckHandledByInterceptor=" + event.isDirtyCheckHandledByInterceptor());
        }

        @Override
        public void onFlush(FlushEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onInitializeCollection(InitializeCollectionEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onLoad(LoadEvent event, LoadType loadType) throws HibernateException {
            log.debug("Event: " + event.getClass().getName() + " on " + event.getEntityClassName());
        }

        @Override
        public void onLock(LockEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onMerge(MergeEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName() + " on " + event.getOriginal() + "/" + event.getResult());
        }

        @Override
        public void onMerge(MergeEvent event, MergeContext copiedAlready) throws HibernateException {
            log.debug("Event: " + event.getClass().getName() + " (copiedAlready) on " + event.getOriginal() + "/" + event.getResult());
        }

        @Override
        public void onPersist(PersistEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName() + " on " + event.getObject().getClass().getName());
        }

        @Override
        public void onPersist(PersistEvent event, PersistContext createdAlready) throws HibernateException {
            log.debug("Event: " + event.getClass().getName() + " on " + event.getObject().getClass().getName());
        }

        @Override
        public boolean requiresPostCommitHandling(EntityPersister persister) {
            return false;
        }

        @Override
        public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onPostRemoveCollection(PostCollectionRemoveEvent event) {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onPostDelete(PostDeleteEvent event) {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onPostInsert(PostInsertEvent event) {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onPostLoad(PostLoadEvent event) {
            log.debug("Event: " + event.getClass().getName() + " on " + event.getEntity());
        }

        @Override
        public void onPostUpdate(PostUpdateEvent event) {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onPreRecreateCollection(PreCollectionRecreateEvent event) {
            log.debug("Event: " + event.getClass().getName() + " on " + event.getAffectedOwnerOrNull().getClass().getName());
        }

        @Override
        public void onPreRemoveCollection(PreCollectionRemoveEvent event) {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
            log.debug("Event: " + event.getClass().getName() + " on " + event.getAffectedOwnerOrNull().getClass().getName());
        }

        @Override
        public boolean onPreDelete(PreDeleteEvent event) {
            log.debug("Event: " + event.getClass().getName());
            return false;
        }

        @Override
        public boolean onPreInsert(PreInsertEvent event) {
            log.debug("Event: " + event.getClass().getName());
            return false;
        }

        @Override
        public void onPreLoad(PreLoadEvent event) {
            log.debug("Event: " + event.getClass().getName() + " on " + event.getEntity().getClass().getName());
        }

        @Override
        public boolean onPreUpdate(PreUpdateEvent event) {
            log.debug("Event: " + event.getClass().getName());
            return false;
        }

        @Override
        public void onRefresh(RefreshEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onRefresh(RefreshEvent event, RefreshContext refreshedAlready) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onReplicate(ReplicateEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onResolveNaturalId(ResolveNaturalIdEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }

        @Override
        public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
            log.debug("Event: " + event.getClass().getName());
        }
    }
}
