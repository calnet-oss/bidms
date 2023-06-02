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

import edu.berkeley.bidms.orm.event.ValidateOnFlush;
import edu.berkeley.bidms.orm.event.ValidateOnLoad;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.FlushEntityEvent;
import org.hibernate.event.spi.FlushEntityEventListener;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;

@Component
public class ValidateableEventListenerConfigurer {

    private final Logger log = LoggerFactory.getLogger(ValidateableEventListenerConfigurer.class);

    private final EntityManagerFactory entityManagerFactory;

    public ValidateableEventListenerConfigurer(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @PostConstruct
    public void init() {
        init(entityManagerFactory.unwrap(SessionFactoryImplementor.class));
    }

    protected void init(SessionFactoryImplementor sessionFactory) {
        // https://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#events-events
        EventListenerRegistry eventListenerRegistry = sessionFactory
                .getServiceRegistry()
                .getService(EventListenerRegistry.class);
        eventListenerRegistry.appendListeners(EventType.POST_LOAD, new ValidateOnLoadEventListener());
        eventListenerRegistry.prependListeners(EventType.FLUSH_ENTITY, new ValidateOnFlushEventListener());
    }

    public static class ValidateOnFlushEventListener implements FlushEntityEventListener {
        private final Logger log = LoggerFactory.getLogger(ValidateOnFlushEventListener.class);

        /**
         * Not sure if event ordering is specified anywhere by Hibernate or
         * JPA, but this is what I observed as of Spring Boot 2.2.6: When
         * FlushMode is COMMIT, when a collection is updated, the order of
         * event receivership is: First the entity owning the collection
         * (dirty or not) has this event triggered, then the entity elements
         * of the collection (dirty or not) have this event triggered (as
         * long as the collection is dirty).
         * <p>
         * This is important if the validators depend on one another.
         * <p>
         * It may not be possible for onFlush validators to change the state
         * of any entity, especially if the entity isn't marked dirty
         * already.  Unexpected behavior may result.
         */
        @Override
        public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
            if (event.getEntity() instanceof ValidateOnFlush) {
                log.trace("calling validator due to flush event on entity " + event.getEntity());
                ((ValidateOnFlush) event.getEntity()).validateOnFlush();
            }
        }
    }

    public static class ValidateOnLoadEventListener implements PostLoadEventListener {
        private final Logger log = LoggerFactory.getLogger(ValidateOnLoadEventListener.class);

        @Override
        public void onPostLoad(PostLoadEvent event) {
            if (event.getEntity() instanceof ValidateOnLoad) {
                log.trace("calling validator due to load event on entity " + event.getEntity());
                ((ValidateOnLoad) event.getEntity()).validateOnLoad();
            }
        }
    }
}
