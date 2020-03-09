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
package edu.berkeley.bidms.app.sgs.executor.ldap;

import edu.berkeley.bidms.app.sgs.config.properties.DirectoryAttributeConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.DirectoryAttributeMetadataConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.LdapConnectionConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.SgsConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.SorConfigProperties;
import edu.berkeley.bidms.app.sgs.executor.Counter;
import edu.berkeley.bidms.app.sgs.executor.EntryContentExtractorException;
import edu.berkeley.bidms.app.sgs.executor.ExecutorConfigurationException;
import edu.berkeley.bidms.app.sgs.executor.ExecutorService;
import edu.berkeley.bidms.app.sgs.executor.QueryEntryCallback;
import edu.berkeley.bidms.app.sgs.executor.SearchExecutor;
import edu.berkeley.bidms.app.sgs.executor.SearchExecutorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;

import javax.naming.NameClassPair;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Provides hash and query implementations for LDAP.
 */
public class LdapSearchExecutor implements SearchExecutor<LdapTemplateExt, LdapHashEntryContent, LdapQueryEntryContent> {
    private final Logger log = LoggerFactory.getLogger(LdapSearchExecutor.class);

    private SgsConfigProperties sgsConfigProperties;
    private ExecutorService executorService;

    public LdapSearchExecutor(SgsConfigProperties sgsConfigProperties, ExecutorService executorService) {
        this.sgsConfigProperties = sgsConfigProperties;
        this.executorService = executorService;
    }

    /**
     * @return A {@link SearchScope} instance that indicates whether to do a
     * one-level "shallow search" at the base or a whether to traverse all
     * subtrees of the base.
     */
    public SearchScope getSearchScope() {
        return SearchScope.ONELEVEL;
    }

    /**
     * @return The operational attribute name for the last modified time
     * attribute.
     */
    public String getModifyTimestampAttributeName() {
        return "modifyTimestamp";
    }

    /**
     * @return The operational attribute name for the creation time
     * attribute.
     */
    public String getCreateTimestampAttributeName() {
        return "createTimestamp";
    }

    /**
     * @param sorConfig SOR configuration
     * @return An instance of {@link LdapTemplateExt} which extends Spring's
     * {@link LdapTemplate}.
     * @throws SearchExecutorException If there was a problem creating the
     *                                 context.
     */
    @Override
    public LdapTemplateExt createContext(SorConfigProperties sorConfig) throws SearchExecutorException {
        try {
            return getLdapTemplate(getLdapConnectionConfig(sorConfig));
        } catch (ExecutorConfigurationException e) {
            throw new SearchExecutorException(e);
        }
    }

    /**
     * Perform a "full mode" LDAP query which queries for all entries
     * matching the search filter within the search base.
     *
     * @param sorConfig       SOR configuration.
     * @param ldapTemplate    The {@link LdapTemplateExt}.
     * @param queryTime       The time the query started.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on each entry as the
     *                        search is performed and each entry in the
     *                        result is iterated.
     * @return An instance of {@link Counter} that indicates quantity of
     * successful entries processed and quantity of failed entry attempts.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any entries from being iterated.
     */
    @Override
    public Counter searchQueryFullMode(SorConfigProperties sorConfig, LdapTemplateExt ldapTemplate, OffsetDateTime queryTime, QueryEntryCallback<LdapQueryEntryContent> callbackHandler) throws SearchExecutorException {
        try {
            return searchQuery(sorConfig, ldapTemplate, queryTime, getFullModeLdapQuery(sorConfig), callbackHandler);
        } catch (ExecutorConfigurationException e) {
            throw new SearchExecutorException(e);
        }
    }

    /**
     * Perform an "individual mode" LDAP query which queries for a single
     * key.
     *
     * @param sorConfig       SOR configuration.
     * @param ldapTemplate    The {@link LdapTemplateExt}.
     * @param queryTime       The time the query started.
     * @param sorObjKey       The primary key to search for.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on a found entry.
     * @return An instance of {@link Counter}.  Since we are only searching
     * for one key, either the success count will be 1 or the failure count
     * will be 1.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any entries from being iterated.
     */
    @Override
    public Counter searchQueryIndividualMode(SorConfigProperties sorConfig, LdapTemplateExt ldapTemplate, OffsetDateTime queryTime, String sorObjKey, QueryEntryCallback<LdapQueryEntryContent> callbackHandler) throws SearchExecutorException {
        try {
            return searchQuery(sorConfig, ldapTemplate, queryTime, getIndividualModeLdapQuery(sorConfig, sorObjKey), callbackHandler);
        } catch (ExecutorConfigurationException e) {
            throw new SearchExecutorException(e);
        }
    }

    /**
     * Perform a "last changed mode" LDAP query which queries for entries
     * that have changed since a certain time.
     *
     * @param sorConfig       SOR configuration.
     * @param ldapTemplate    The {@link LdapTemplateExt}.
     * @param queryTime       The time the query started.
     * @param lastQueryTime   Query for changed entries since this
     *                        timestamp.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on each entry as the
     *                        search is performed and each entry in the
     *                        result is iterated.
     * @return An instance of {@link Counter} that indicates quantity of
     * successful entries processed and quantity of failed entry attempts.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any entries from being iterated.
     */
    @Override
    public Counter searchQueryLastChangedMode(SorConfigProperties sorConfig, LdapTemplateExt ldapTemplate, OffsetDateTime queryTime, Timestamp lastQueryTime, QueryEntryCallback<LdapQueryEntryContent> callbackHandler) throws SearchExecutorException {
        try {
            return searchQuery(sorConfig, ldapTemplate, queryTime, getLastChangedModeLdapQuery(sorConfig, lastQueryTime), callbackHandler);
        } catch (ExecutorConfigurationException e) {
            throw new SearchExecutorException(e);
        }
    }

    /**
     * Perform a "full mode" LDAP hash query which queries for all entries
     * matching the search filter within the search base.
     *
     * @param sorConfig       SOR configuration.
     * @param ldapTemplate    The {@link LdapTemplateExt}.
     * @param hashTime        The time the hash query started.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on each entry as the
     *                        search is performed and each entry in the
     *                        result is iterated.
     * @return An instance of {@link Counter} that indicates quantity of
     * successful entries processed and quantity of failed entry attempts.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any entries from being iterated.
     */
    @Override
    public Counter searchHashFullMode(SorConfigProperties sorConfig, LdapTemplateExt ldapTemplate, OffsetDateTime hashTime, QueryEntryCallback<LdapHashEntryContent> callbackHandler) throws SearchExecutorException {
        try {
            return searchHash(sorConfig, ldapTemplate, hashTime, getFullModeLdapQuery(sorConfig), callbackHandler);
        } catch (ExecutorConfigurationException e) {
            throw new SearchExecutorException(e);
        }
    }

    /**
     * Perform an "individual mode" LDAP hash query which queries for a
     * single key.
     *
     * @param sorConfig       SOR configuration.
     * @param ldapTemplate    The {@link LdapTemplateExt}.
     * @param hashTime        The time the hash query started.
     * @param sorObjKey       The primary key to search for.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on a found entry.
     * @return An instance of {@link Counter}.  Since we are only searching
     * for one key, either the success count will be 1 or the failure count
     * will be 1.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any entries from being iterated.
     */
    @Override
    public Counter searchHashIndividualMode(SorConfigProperties sorConfig, LdapTemplateExt ldapTemplate, OffsetDateTime hashTime, String sorObjKey, QueryEntryCallback<LdapHashEntryContent> callbackHandler) throws SearchExecutorException {
        try {
            return searchHash(sorConfig, ldapTemplate, hashTime, getIndividualModeLdapQuery(sorConfig, sorObjKey), callbackHandler);
        } catch (ExecutorConfigurationException e) {
            throw new SearchExecutorException(e);
        }
    }

    /**
     * Perform a "last changed mode" LDAP hash query which queries for
     * entries that have changed since a certain time.
     *
     * @param sorConfig       SOR configuration.
     * @param ldapTemplate    The {@link LdapTemplateExt}.
     * @param hashTime        The time the hash query started.
     * @param lastQueryTime   Query for changed entries since this
     *                        timestamp.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on each entry as the
     *                        search is performed and each entry in the
     *                        result is iterated.
     * @return An instance of {@link Counter} that indicates quantity of
     * successful entries processed and quantity of failed entry attempts.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any entries from being iterated.
     */
    @Override
    public Counter searchHashLastChangedMode(SorConfigProperties sorConfig, LdapTemplateExt ldapTemplate, OffsetDateTime hashTime, Timestamp lastQueryTime, QueryEntryCallback<LdapHashEntryContent> callbackHandler) throws SearchExecutorException {
        try {
            return searchHash(sorConfig, ldapTemplate, hashTime, getLastChangedModeLdapQuery(sorConfig, lastQueryTime), callbackHandler);
        } catch (ExecutorConfigurationException e) {
            throw new SearchExecutorException(e);
        }
    }

    private LdapConnectionConfigProperties getLdapConnectionConfig(SorConfigProperties sorConfig) throws ExecutorConfigurationException {
        LdapConnectionConfigProperties ldapConnectionConfig = sgsConfigProperties.getConnections().getLdap().get(sorConfig.getConnectionName());
        if (ldapConnectionConfig == null) {
            throw new ExecutorConfigurationException("No connection with the connection-name of " + sorConfig.getConnectionName() + " is configured");
        }
        return ldapConnectionConfig;
    }

    private Counter searchQuery(SorConfigProperties sorConfig, LdapTemplate ldapTemplate, OffsetDateTime queryTime, LdapQuery query, QueryEntryCallback<LdapQueryEntryContent> callbackHandler) throws ExecutorConfigurationException {
        final DirContextQueryExtractor entryExtractor = getQueryEntryExtractor(sorConfig);
        final Counter counter = new Counter();
        ldapTemplate.search(
                query.base(),
                query.filter().encode(),
                ldapQueryToSearchControls(query),
                (NameClassPair nameClassPair) -> {
                    DirContextAdapter dirCtx = (DirContextAdapter) ((SearchResult) nameClassPair).getObject();

                    LdapQueryEntryContent queryEntryContent;
                    try {
                        queryEntryContent = entryExtractor.extractContent(sorConfig, queryTime, dirCtx);
                    } catch (EntryContentExtractorException | RuntimeException e) {
                        log.error("Error parsing the entry for LDAP DN=" + dirCtx.getDn(), e);
                        counter.incrementFailCount();
                        return;
                    }

                    try {
                        callbackHandler.handle(queryEntryContent);
                    } catch (RuntimeException e) {
                        log.error("There was an exception handling the query entry content", e);
                        counter.incrementFailCount();
                        return;
                    }
                    counter.incrementSuccessCount();
                }
        );
        return counter;
    }

    private Counter searchHash(SorConfigProperties sorConfig, LdapTemplate ldapTemplate, OffsetDateTime hashTime, LdapQuery query, QueryEntryCallback<LdapHashEntryContent> callbackHandler) throws ExecutorConfigurationException {
        final DirContextHashExtractor entryExtractor = getHashEntryExtractor(sorConfig);
        final Counter counter = new Counter();
        ldapTemplate.search(
                query.base(),
                query.filter().encode(),
                ldapQueryToSearchControls(query),
                (NameClassPair nameClassPair) -> {
                    DirContextAdapter dirCtx = (DirContextAdapter) ((SearchResult) nameClassPair).getObject();

                    LdapHashEntryContent hashEntryContent;
                    try {
                        hashEntryContent = entryExtractor.extractContent(sorConfig, hashTime, dirCtx);
                    } catch (EntryContentExtractorException | RuntimeException e) {
                        log.error("Error parsing the entry for LDAP DN=" + dirCtx.getDn(), e);
                        counter.incrementFailCount();
                        return;
                    }

                    try {
                        callbackHandler.handle(hashEntryContent);
                    } catch (RuntimeException e) {
                        log.error("There was an exception handling the hash entry content", e);
                        counter.incrementFailCount();
                        return;
                    }
                    counter.incrementSuccessCount();
                }
        );
        return counter;
    }

    private LdapQuery getFullModeLdapQuery(SorConfigProperties sorConfig) throws ExecutorConfigurationException {
        String searchBase = getSearchBase(sorConfig);
        LdapQueryBuilder _query = searchBase != null ? LdapQueryBuilder.query().base(searchBase) : LdapQueryBuilder.query();
        return _query
                .searchScope(getSearchScope())
                .attributes(getAttributeNames(sorConfig))
                .where("objectClass").is("person")
                .and("uid").isPresent();
    }

    private LdapQuery getIndividualModeLdapQuery(SorConfigProperties sorConfig, String sorObjKey) throws ExecutorConfigurationException {
        String searchBase = getSearchBase(sorConfig);
        LdapQueryBuilder _query = searchBase != null ? LdapQueryBuilder.query().base(searchBase) : LdapQueryBuilder.query();
        return _query
                .searchScope(getSearchScope())
                .attributes(getAttributeNames(sorConfig))
                .where("objectClass").is("person")
                .and("uid").is(sorObjKey);
    }

    private LdapQuery getLastChangedModeLdapQuery(SorConfigProperties sorConfig, Date lastQueryTime) throws ExecutorConfigurationException {
        String searchBase = getSearchBase(sorConfig);
        String timeMarker = convertDateToLdapTime(lastQueryTime);
        LdapQueryBuilder _query = searchBase != null ? LdapQueryBuilder.query().base(searchBase) : LdapQueryBuilder.query();
        return _query
                .searchScope(getSearchScope())
                .attributes(getAttributeNames(sorConfig))
                .where("objectClass").is("person")
                .and("uid").isPresent()
                .and(LdapQueryBuilder.query().where(getModifyTimestampAttributeName()).gte(timeMarker).or(getCreateTimestampAttributeName()).gte(timeMarker));
    }

    private DirContextQueryExtractor getQueryEntryExtractor(SorConfigProperties sorConfig) throws ExecutorConfigurationException {
        DirContextQueryExtractor extractor = executorService.getConfiguredQueryEntryExtractorBean(sorConfig, DirContextQueryExtractor.class);
        return extractor != null ? extractor : new DirContextQueryExtractor(getCreateTimestampAttributeName(), getModifyTimestampAttributeName(), getDirectoryAttributeMetadata(sorConfig));
    }

    private DirContextHashExtractor getHashEntryExtractor(SorConfigProperties sorConfig) throws ExecutorConfigurationException {
        DirContextHashExtractor extractor = executorService.getConfiguredQueryEntryExtractorBean(sorConfig, DirContextHashExtractor.class);
        return extractor != null ? extractor : new DirContextHashExtractor(sgsConfigProperties.getConnections().getLdap().get(sorConfig.getConnectionName()).getDialect(), getCreateTimestampAttributeName(), getModifyTimestampAttributeName());
    }

    private ContextSource getContextSource(LdapConnectionConfigProperties ldapConnectionConfig) {
        LdapContextSource cs = new LdapContextSource();
        cs.setUserDn(ldapConnectionConfig.getBindDn());
        cs.setPassword(ldapConnectionConfig.getBindPassword());
        cs.setUrl(ldapConnectionConfig.getUrl());
        cs.setBase(ldapConnectionConfig.getBaseDn());
        cs.setBaseEnvironmentProperties(Map.of("com.sun.jndi.ldap.read.timeout", "3600000")); // 60 minutes - needs to be long for big batch queries
        cs.afterPropertiesSet();
        return cs;
    }

    private LdapTemplateExt getLdapTemplate(LdapConnectionConfigProperties ldapConnectionConfig) {
        return new LdapTemplateExt(getContextSource(ldapConnectionConfig));
    }

    /**
     * Create a SearchControls object from a LdapQuery.
     *
     * @param query LdapQuery object
     * @return The SearchControls object
     */
    private SearchControls ldapQueryToSearchControls(LdapQuery query) {
        SearchControls controls = new SearchControls();
        controls.setSearchScope((query.searchScope() != null ? query.searchScope().getId() : SearchControls.SUBTREE_SCOPE));
        controls.setTimeLimit((query.timeLimit() != null ? query.timeLimit() : 0));
        controls.setCountLimit((query.countLimit() != null ? query.countLimit() : 0));
        controls.setReturningObjFlag(true);
        controls.setReturningAttributes(query.attributes());
        return controls;
    }

    private Map<String, DirectoryAttributeConfigProperties> getDirectoryAttributeMetadata(SorConfigProperties sorConfig) throws ExecutorConfigurationException {
        // find the connection configuration
        LdapConnectionConfigProperties ldapConnectionConfig = sgsConfigProperties.getConnections().getLdap().get(sorConfig.getConnectionName());
        if (ldapConnectionConfig == null) {
            throw new ExecutorConfigurationException("No connection with the connection-name of " + sorConfig.getConnectionName() + " is configured");
        }

        DirectoryAttributeMetadataConfigProperties metaData = sgsConfigProperties.getDirectoryAttributeMetadata().get(ldapConnectionConfig.getMetadataSetName());
        return metaData != null ? metaData.getAttributes() : null;
    }

    private String[] getAttributeNames(SorConfigProperties sorConfig) throws ExecutorConfigurationException {
        Set<String> keySet = getDirectoryAttributeMetadata(sorConfig).keySet();
        String[] keys = new String[keySet.size()];
        return keySet.toArray(keys);
    }

    private static final String LDAP_DATE_FORMAT = "yyyyMMddHHmmss'Z'";

    /**
     * Convert Date objects to timestamp strings used in LDAP filters.
     */
    private String convertDateToLdapTime(Date date) {
        SimpleDateFormat ldapSdf = new SimpleDateFormat(LDAP_DATE_FORMAT);
        // LDAP timestamps are in UTC/GMT
        ldapSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return date != null ? ldapSdf.format(date) : null;
    }

    private String getSearchBase(SorConfigProperties sorConfig) {
        return sorConfig.getLdap() != null ? sorConfig.getLdap().getSearchBase() : null;
    }
}
