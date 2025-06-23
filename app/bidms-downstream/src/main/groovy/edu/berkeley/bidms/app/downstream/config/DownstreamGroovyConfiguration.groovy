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
package edu.berkeley.bidms.app.downstream.config

import edu.berkeley.bidms.app.common.config.properties.BidmsConfigProperties
import edu.berkeley.bidms.app.downstream.config.properties.DownstreamConfigProperties
import edu.berkeley.bidms.app.downstream.service.ldap.LdapDownstreamObjectUpdaterService
import edu.berkeley.bidms.connector.ldap.LdapConnector
import edu.berkeley.bidms.downstream.jms.DownstreamProvisionJmsTemplate
import edu.berkeley.bidms.downstream.ldap.LdapDeleteEventLoggingCallback
import edu.berkeley.bidms.downstream.ldap.LdapInsertEventLoggingCallback
import edu.berkeley.bidms.downstream.ldap.LdapPersistCompletionEventLoggingCallback
import edu.berkeley.bidms.downstream.ldap.LdapRenameEventLoggingCallback
import edu.berkeley.bidms.downstream.ldap.LdapUniqueIdentifierEventProcessingCallback
import edu.berkeley.bidms.downstream.ldap.LdapUpdateEventLoggingCallback
import edu.berkeley.bidms.downstream.ldap.MainEntryUidObjectDefinition
import jakarta.jms.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.ldap.pool2.factory.PoolConfig
import org.springframework.ldap.pool2.factory.PooledContextSource
import org.springframework.ldap.pool2.validation.DefaultDirContextValidator

@Configuration
class DownstreamGroovyConfiguration {

    @Value('${bidms.downstream.app-name}')
    private String APP_NAME

    private BidmsConfigProperties bidmsConfigProperties
    private DownstreamConfigProperties downstreamConfigProperties

    DownstreamGroovyConfiguration(BidmsConfigProperties bidmsConfigProperties, DownstreamConfigProperties downstreamConfigProperties) {
        this.bidmsConfigProperties = bidmsConfigProperties
        this.downstreamConfigProperties = downstreamConfigProperties
    }

    private static String[] stringArray(String... args) {
        def arr = new String[args.length]
        for (int i = 0; i < args.length; i++) {
            arr[i] = args[i]
        }
        return arr
    }

    /***
     *** LDAP CONNECTION CONFIGURATION
     ***/

    @Bean(name = "ldapContextSource")
    LdapContextSource getLdapContextSource() {
        def bean = new LdapContextSource();
        bean.setUserDn(downstreamConfigProperties.getLdap().getBindDn());
        bean.setPassword(downstreamConfigProperties.getLdap().getBindPassword());
        bean.setUrl(downstreamConfigProperties.getLdap().getUrl().toString());
        return bean;
    }

    @Bean(name = "ldapPoolConfig")
    PoolConfig getLdapPoolConfig() {
        // http://docs.spring.io/spring-ldap/docs/current/reference/#pool2-configuration
        // http://docs.spring.io/spring-ldap/docs/current/apidocs/org/springframework/ldap/pool2/factory/PoolConfig.html
        def bean = new PoolConfig();
        bean.setBlockWhenExhausted(true);
        bean.setMaxTotal(40);
        bean.setMaxTotalPerKey(40);
        bean.setMaxIdlePerKey(40);
        bean.setMaxWaitMillis(20000);
        bean.setTimeBetweenEvictionRunsMillis(120000);
        bean.setNumTestsPerEvictionRun(5);
        bean.setMinEvictableIdleTimeMillis(5 * 60 * 1000);
        bean.setTestWhileIdle(true);
        return bean;
    }

    @Bean(name = "pooledLdapContextSource")
    PooledContextSource getPooledLdapContextSource(
            @Qualifier("ldapPoolConfig") PoolConfig poolConfig,
            @Qualifier("ldapContextSource") LdapContextSource contextSource
    ) {
        def contextValidator = new DefaultDirContextValidator();
        contextValidator.setBase(downstreamConfigProperties.getLdap().getBindDn());
        def bean = new PooledContextSource(poolConfig);
        bean.setContextSource(contextSource);
        bean.setDirContextValidator(contextValidator);
        return bean;
    }

    @Bean(name = "ldapUniqueIdentifierEventProcessingCallback")
    LdapUniqueIdentifierEventProcessingCallback getLdapUniqueIdentifierEventProcessingCallback(LdapDownstreamObjectUpdaterService ldapDownstreamObjectUpdaterService) {
        def cb = new LdapUniqueIdentifierEventProcessingCallback();
        cb.setLdapDownstreamObjectUpdaterService(ldapDownstreamObjectUpdaterService);
        return cb;
    }

    @Bean(name = "ldapPersistCompletionEventLoggingCallback")
    LdapPersistCompletionEventLoggingCallback getLdapPersistCompletionEventLoggingCallback(@Qualifier("pooledLdapContextSource") PooledContextSource contextSource) {
        def cb = new LdapPersistCompletionEventLoggingCallback();
        cb.setPooledContextSource(contextSource);
        return cb;
    }

    @Bean(name = "ldapConnector", initMethod = "start", destroyMethod = "stop")
    LdapConnector getLdapConnector(
            @Qualifier("pooledLdapContextSource") PooledContextSource pooledLdapContextSource,
            @Qualifier("ldapUniqueIdentifierEventProcessingCallback") LdapUniqueIdentifierEventProcessingCallback ldapUniqueIdentifierEventProcessingCallback,
            @Qualifier("ldapPersistCompletionEventLoggingCallback") LdapPersistCompletionEventLoggingCallback ldapPersistCompletionEventLoggingCallback
    ) {
        LdapConnector c = new LdapConnector();
        c.setContextSource(pooledLdapContextSource);
        c.setIsSynchronousCallback(downstreamConfigProperties.getLdapConnector().isSynchronousCallback());
        c.setDeleteEventCallbacks(List.of(new LdapDeleteEventLoggingCallback(APP_NAME)));
        c.setInsertEventCallbacks(List.of(new LdapInsertEventLoggingCallback(APP_NAME)));
        c.setRenameEventCallbacks(List.of(new LdapRenameEventLoggingCallback(APP_NAME)));
        c.setUpdateEventCallbacks(List.of(new LdapUpdateEventLoggingCallback(APP_NAME)));
        c.setUniqueIdentifierEventCallbacks(List.of(ldapUniqueIdentifierEventProcessingCallback));
        c.setPersistCompletionEventCallbacks(List.of(ldapPersistCompletionEventLoggingCallback));
        return c;
    }

    @Bean(name = "ldapTemplate")
    LdapTemplate getLdapTemplate(@Qualifier("pooledLdapContextSource") PooledContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

    @Bean(name = "mainEntryUidObjectDefinition")
    MainEntryUidObjectDefinition getMainEntryUidObjectDefinition() {
        def bean = new MainEntryUidObjectDefinition();
        bean.setSearchBase(downstreamConfigProperties.getLdap().getSearchBase());
        bean.setObjectClass("person");
        bean.setKeepExistingAttributesWhenUpdating(true);
        bean.setRemoveDuplicatePrimaryKeys(true);
        bean.setDynamicAttributeNames(stringArray("objectClass.APPEND"));
        return bean;
    }

    @Bean("provDownstreamProvisionJmsTemplateForDownstreamApp")
    DownstreamProvisionJmsTemplate getDownstreamProvisionJmsTemplate(ApplicationContext applicationContext) {
        return new DownstreamProvisionJmsTemplate(applicationContext.getBean(downstreamConfigProperties.getJms().getDownstream().getJmsConnectionFactoryBeanName(), ConnectionFactory));
    }
}
