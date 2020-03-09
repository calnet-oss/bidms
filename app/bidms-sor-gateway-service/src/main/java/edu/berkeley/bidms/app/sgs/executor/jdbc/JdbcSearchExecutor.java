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
package edu.berkeley.bidms.app.sgs.executor.jdbc;

import edu.berkeley.bidms.app.sgs.config.freemarker.FreemarkerConfigurationForSqlTemplates;
import edu.berkeley.bidms.app.sgs.config.properties.JdbcDataSourceConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.SgsConfigProperties;
import edu.berkeley.bidms.app.sgs.config.properties.SorConfigProperties;
import edu.berkeley.bidms.app.sgs.executor.Counter;
import edu.berkeley.bidms.app.sgs.executor.EntryContentExtractorException;
import edu.berkeley.bidms.app.sgs.executor.ExecutorConfigurationException;
import edu.berkeley.bidms.app.sgs.executor.ExecutorService;
import edu.berkeley.bidms.app.sgs.executor.HashEntryContent;
import edu.berkeley.bidms.app.sgs.executor.QueryEntryCallback;
import edu.berkeley.bidms.app.sgs.executor.SearchExecutor;
import edu.berkeley.bidms.app.sgs.executor.SearchExecutorException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.LongSummaryStatistics;

/**
 * Provides hash and query implementations for JDBC.
 */
public class JdbcSearchExecutor implements SearchExecutor<Connection, HashEntryContent<Void>, JdbcQueryRowContent> {
    private final Logger log = LoggerFactory.getLogger(JdbcSearchExecutor.class);

    private SgsConfigProperties sgsConfigProperties;
    private ExecutorService executorService;
    private FreemarkerConfigurationForSqlTemplates freemarkerConfiguration;

    public JdbcSearchExecutor(SgsConfigProperties sgsConfigProperties, ExecutorService executorService, FreemarkerConfigurationForSqlTemplates freemarkerConfiguration) {
        this.sgsConfigProperties = sgsConfigProperties;
        this.executorService = executorService;
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    /**
     * @param sorConfig SOR configuration.
     * @return A JDBC {@link Connection} that will be used during the course
     * of the query.
     * @throws SearchExecutorException If an error occurred creating the
     *                                 connection.
     */
    @Override
    public Connection createContext(SorConfigProperties sorConfig) throws SearchExecutorException {
        try {
            return getSorDataSource(getDataSourceConfig(sorConfig)).getConnection();
        } catch (SQLException | ClassNotFoundException | ExecutorConfigurationException e) {
            throw new SearchExecutorException(e);
        }
    }

    /**
     * Perform a "full mode" JDBC query which queries for all entries using
     * the query SQL template.
     *
     * @param sorConfig       SOR configuration.
     * @param conn            The JDBC {@link Connection}.
     * @param queryTime       The time the query started but be aware this
     *                        may be ignored if the queryTime is embedded in
     *                        the query content (which is the case if {@link
     *                        ClobColumnExtractor} is being used).
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on each entry as the
     *                        search is performed and each entry in the
     *                        result is iterated.
     * @return An instance of {@link Counter} that indicates quantity of
     * successful entries processed and quantity of failed entry attempts.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any rows from being iterated.
     */
    @Override
    public Counter searchQueryFullMode(SorConfigProperties sorConfig, Connection conn, OffsetDateTime queryTime, QueryEntryCallback<JdbcQueryRowContent> callbackHandler) throws SearchExecutorException {
        try (PreparedStatement ps = conn.prepareStatement(getFullModeQuerySql(sorConfig))) {
            return searchQuery(sorConfig, queryTime, ps, callbackHandler);
        } catch (SQLException e) {
            throw new SearchExecutorException(e);
        }
    }

    /**
     * Perform an "individual mode" JDBC query which queries for a single key
     * using the query SQL template.
     *
     * @param sorConfig       SOR configuration.
     * @param conn            The JDBC {@link Connection}.
     * @param queryTime       The time the query started but be aware this
     *                        may be ignored if the queryTime is embedded in
     *                        the query content (which is the case if {@link
     *                        ClobColumnExtractor} is being used).
     * @param sorObjKey       The primary key to search for.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on a found entry.
     * @return An instance of {@link Counter}.  Since we are only searching
     * for one key, either the success count will be 1 or the failure count
     * will be 1.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any rows from being iterated.
     */
    @Override
    public Counter searchQueryIndividualMode(SorConfigProperties sorConfig, Connection conn, OffsetDateTime queryTime, String sorObjKey, QueryEntryCallback<JdbcQueryRowContent> callbackHandler) throws SearchExecutorException {
        try (PreparedStatement ps = conn.prepareStatement(getIndividualModeQuerySql(sorConfig))) {
            ps.setString(1, sorObjKey);
            return searchQuery(sorConfig, queryTime, ps, callbackHandler);
        } catch (SQLException e) {
            throw new SearchExecutorException(e);
        }
    }

    /**
     * Perform a "last changed mode" JDBC query which queries for rows that
     * have changed since a certain time using the query SQL template.
     *
     * @param sorConfig       SOR configuration.
     * @param conn            The JDBC {@link Connection}.
     * @param queryTime       The time the query started but be aware this
     *                        may be ignored if the queryTime is embedded in
     *                        the query content (which is the case if {@link
     *                        ClobColumnExtractor} is being used).
     * @param lastQueryTime   Query for changed entries since this
     *                        timestamp.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on each entry as the
     *                        search is performed and each entry in the
     *                        result is iterated.
     * @return An instance of {@link Counter} that indicates quantity of
     * successful entries processed and quantity of failed entry attempts.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any rows from being iterated.
     */
    @Override
    public Counter searchQueryLastChangedMode(SorConfigProperties sorConfig, Connection conn, OffsetDateTime queryTime, Timestamp lastQueryTime, QueryEntryCallback<JdbcQueryRowContent> callbackHandler) throws SearchExecutorException {
        try (PreparedStatement ps = conn.prepareStatement(getLastChangedModeQuerySql(sorConfig))) {
            ps.setTimestamp(1, lastQueryTime);
            return searchQuery(sorConfig, queryTime, ps, callbackHandler);
        } catch (SQLException e) {
            throw new SearchExecutorException(e);
        }
    }

    /**
     * Perform a "full mode" JDBC hash query which queries for all entries
     * using the hash SQL template.
     *
     * @param sorConfig       SOR configuration.
     * @param conn            The JDBC {@link Connection}.
     * @param hashTime        The time the hash query started.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on each entry as the
     *                        search is performed and each entry in the
     *                        result is iterated.
     * @return An instance of {@link Counter} that indicates quantity of
     * successful entries processed and quantity of failed entry attempts.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any rows from being iterated.
     */
    @Override
    public Counter searchHashFullMode(SorConfigProperties sorConfig, Connection conn, OffsetDateTime hashTime, QueryEntryCallback<HashEntryContent<Void>> callbackHandler) throws SearchExecutorException {
        try (PreparedStatement ps = conn.prepareStatement(getFullModeHashSql(sorConfig))) {
            return searchHash(sorConfig, hashTime, ps, callbackHandler);
        } catch (SQLException e) {
            throw new SearchExecutorException(e);
        }
    }

    /**
     * Perform an "individual mode" JDBC hash query which queries for a
     * single key using the hash SQL template.
     *
     * @param sorConfig       SOR configuration.
     * @param conn            The JDBC {@link Connection}.
     * @param hashTime        The time the hash query started.
     * @param sorObjKey       The primary key to search for.
     * @param callbackHandler An instance of a {@link QueryEntryCallback}
     *                        that performs operations on a found entry.
     * @return An instance of {@link Counter}.  Since we are only searching
     * for one key, either the success count will be 1 or the failure count
     * will be 1.
     * @throws SearchExecutorException If an error occurred that prevented
     *                                 any rows from being iterated.
     */
    @Override
    public Counter searchHashIndividualMode(SorConfigProperties sorConfig, Connection conn, OffsetDateTime hashTime, String sorObjKey, QueryEntryCallback<HashEntryContent<Void>> callbackHandler) throws SearchExecutorException {
        try (PreparedStatement ps = conn.prepareStatement(getIndividualModeHashSql(sorConfig, sorObjKey))) {
            ps.setString(1, sorObjKey);
            return searchHash(sorConfig, hashTime, ps, callbackHandler);
        } catch (SQLException e) {
            throw new SearchExecutorException(e);
        }
    }

    /**
     * Perform a "last changed mode" JDBC hash query which queries for rows
     * that have changed since a certain time using the hash SQL template.
     *
     * @param sorConfig       SOR configuration.
     * @param conn            The JDBC {@link Connection}.
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
     *                                 any rows from being iterated.
     */
    @Override
    public Counter searchHashLastChangedMode(SorConfigProperties sorConfig, Connection conn, OffsetDateTime hashTime, Timestamp lastQueryTime, QueryEntryCallback<HashEntryContent<Void>> callbackHandler) throws SearchExecutorException {
        try (PreparedStatement ps = conn.prepareStatement(getLastChangedModeHashSql(sorConfig, lastQueryTime))) {
            ps.setTimestamp(1, lastQueryTime);
            return searchHash(sorConfig, hashTime, ps, callbackHandler);
        } catch (SQLException e) {
            throw new SearchExecutorException(e);
        }
    }

    private Counter searchQuery(SorConfigProperties sorConfig, OffsetDateTime queryTime, PreparedStatement ps, QueryEntryCallback<JdbcQueryRowContent> callbackHandler) throws SearchExecutorException {
        try {
            final ClobColumnExtractor entryExtractor = getQueryEntryExtractor(sorConfig);
            try (ResultSet rs = ps.executeQuery()) {
                final int sorObjKeyIdx = 1;
                final int objIdx = 2;
                LongSummaryStatistics queryRowAvgTime = new LongSummaryStatistics();
                LongSummaryStatistics persistAvgTime = new LongSummaryStatistics();
                LongSummaryStatistics totalRowAvgTime = new LongSummaryStatistics();
                long rowSt;
                final Counter counter = new Counter();
                while ((rowSt = System.nanoTime()) > 0 && rs.next()) {
                    queryRowAvgTime.accept(Math.floorDiv(System.nanoTime() - rowSt, 1000));

                    JdbcQueryRowContent queryRowContent;
                    try {
                        queryRowContent = entryExtractor.extractContent(sorConfig, objIdx, rs);
                    } catch (EntryContentExtractorException | RuntimeException e) {
                        log.warn("Could not extract object content for sorObjKey=" + rs.getString(sorObjKeyIdx), e);
                        counter.incrementFailCount();
                        continue;
                    }

                    // If the key already exists in SORObject, it's an update, otherwise it's a create.
                    long persistSt = System.nanoTime();
                    try {
                        callbackHandler.handle(queryRowContent);
                    } catch (RuntimeException e) {
                        log.warn("There was an exception handling the query row content", e);
                        counter.incrementFailCount();
                        continue;
                    }
                    counter.incrementSuccessCount();
                    long rowEnd = System.nanoTime();
                    persistAvgTime.accept(Math.floorDiv(rowEnd - persistSt, 1000));
                    totalRowAvgTime.accept(Math.floorDiv(rowEnd - rowSt, 1000));

                    if (counter.getSuccessCount() > 0 && counter.getSuccessCount() % 10000 == 0) {
                        logStatistics(sorConfig.getSorName(), counter.getSuccessCount(), queryRowAvgTime, persistAvgTime, totalRowAvgTime);
                    }
                }
                return counter;
            } catch (SQLException e) {
                throw new SearchExecutorException(e);
            }
        } catch (ExecutorConfigurationException e) {
            throw new SearchExecutorException(e);
        }
    }

    private Counter searchHash(SorConfigProperties sorConfig, OffsetDateTime hashTime, PreparedStatement ps, QueryEntryCallback<HashEntryContent<Void>> callbackHandler) throws SearchExecutorException {
        try (ResultSet rs = ps.executeQuery()) {
            int sorObjKeyColumnIdx = rs.findColumn("sorObjKey");
            int hashColumnIdx = rs.findColumn("hash");
            Integer timeMarkerColumnIdx = null;
            if (sorConfig.isHashQueryTimestampSupported()) {
                // When the timestamp is supported, the timeMarker must be present.
                timeMarkerColumnIdx = rs.findColumn("timeMarker");
            }

            ResultRowHashExtractor entryExtractor = new ResultRowHashExtractor(sorObjKeyColumnIdx, hashColumnIdx, timeMarkerColumnIdx);
            Counter counter = new Counter();
            while (rs.next()) {
                HashEntryContent hashEntryContent;
                try {
                    hashEntryContent = entryExtractor.extractContent(sorConfig, hashTime, rs);
                } catch (EntryContentExtractorException | RuntimeException e) {
                    log.warn("Error parsing the entry for sorName=" + sorConfig.getSorName() + ", sorObjKey=" + rs.getString(sorObjKeyColumnIdx), e);
                    counter.incrementFailCount();
                    continue;
                }

                try {
                    callbackHandler.handle(hashEntryContent);
                } catch (RuntimeException e) {
                    log.warn("There was an exception handling the hash row content", e);
                    counter.incrementFailCount();
                    continue;
                }
                counter.incrementSuccessCount();
            }
            return counter;
        } catch (SQLException e) {
            throw new SearchExecutorException(e);
        }
    }

    private String getFullModeQuerySql(SorConfigProperties sorConfig) throws SearchExecutorException {
        return getQueryExecutorSql(
                sorConfig.getJdbc().getQueryExecutorSqlTemplate(),
                new SqlTemplateData(sorConfig.getJdbc().getTemplateProperties())
        );
    }

    private String getIndividualModeQuerySql(SorConfigProperties sorConfig) throws SearchExecutorException {
        return getQueryExecutorSql(
                sorConfig.getJdbc().getQueryExecutorSqlTemplate(),
                new SqlTemplateData("WHERE sorObjKey = ?", sorConfig.getJdbc().getTemplateProperties())
        );
    }

    private String getLastChangedModeQuerySql(SorConfigProperties sorConfig) throws SearchExecutorException {
        return getQueryExecutorSql(
                sorConfig.getJdbc().getQueryExecutorSqlTemplate(),
                new SqlTemplateData("WHERE timeMarker >= ?", sorConfig.getJdbc().getTemplateProperties())
        );
    }


    private String getFullModeHashSql(SorConfigProperties sorConfig) throws SearchExecutorException {
        return getHashExecutorSql(
                sorConfig.getJdbc().getHashExecutorSqlTemplate(),
                new SqlTemplateData(sorConfig.getJdbc().getTemplateProperties())
        );
    }

    private String getIndividualModeHashSql(SorConfigProperties sorConfig, String sorObjKey) throws SearchExecutorException {
        return getHashExecutorSql(
                sorConfig.getJdbc().getHashExecutorSqlTemplate(),
                new SqlTemplateData("WHERE sorObjKey = ?", sorConfig.getJdbc().getTemplateProperties())
        );
    }

    private String getLastChangedModeHashSql(SorConfigProperties sorConfig, Date lastQueryTime) throws SearchExecutorException {
        return getHashExecutorSql(
                sorConfig.getJdbc().getHashExecutorSqlTemplate(),
                new SqlTemplateData("WHERE timeMarker >= ?", sorConfig.getJdbc().getTemplateProperties())
        );
    }

    private ClobColumnExtractor getQueryEntryExtractor(SorConfigProperties sorConfig) throws ExecutorConfigurationException {
        ClobColumnExtractor extractor = executorService.getConfiguredQueryEntryExtractorBean(sorConfig, ClobColumnExtractor.class);
        return extractor != null ? extractor : new ClobColumnExtractor();
    }

    private void logStatistics(String sorName, int count, LongSummaryStatistics queryRowAvgTime, LongSummaryStatistics persistAvgTime, LongSummaryStatistics totalRowAvgTime) {
        final long queryAvg = Math.round(queryRowAvgTime.getAverage());
        final long persistAvg = Math.round(persistAvgTime.getAverage());
        final long totalAvg = Math.round(totalRowAvgTime.getAverage());
        final long otherAvg = totalAvg - queryAvg - persistAvg;

        final long queryPercent = Math.floorDiv(queryAvg * 100, totalAvg);
        final long persistPercent = Math.floorDiv(persistAvg * 100, totalAvg);
        final long otherPercent = 100 - queryPercent - persistPercent;

        log.info(
                "For " + sorName + ", row " + count +
                        ", query avg=" + queryAvg + " (" + queryPercent + "%)" +
                        ", persist avg=" + persistAvg + " (" + persistPercent + "%)" +
                        ", other avg=" + otherAvg + " (" + otherPercent + "%)" +
                        ", total row avg=" + totalAvg
        );
    }

    private String getQueryExecutorSql(String queryExecutorSqlTemplateFile, SqlTemplateData sqlTemplateData) throws SearchExecutorException {
        try {
            return parseSqlTemplateFile(freemarkerConfiguration, queryExecutorSqlTemplateFile, sqlTemplateData);
        } catch (IOException | TemplateException e) {
            throw new SearchExecutorException(e);
        }
    }

    private String getHashExecutorSql(String hashExecutorSqlTemplateFile, SqlTemplateData sqlTemplateData) throws SearchExecutorException {
        try {
            return parseSqlTemplateFile(freemarkerConfiguration, hashExecutorSqlTemplateFile, sqlTemplateData);
        } catch (IOException | TemplateException e) {
            throw new SearchExecutorException(e);
        }
    }

    private DataSource getSorDataSource(JdbcDataSourceConfigProperties jdbcDataSourceConfig) throws ClassNotFoundException, SQLException {
        Class.forName(jdbcDataSourceConfig.getDriverClassName());
        Driver driver = DriverManager.getDriver(jdbcDataSourceConfig.getUrl());
        return new SimpleDriverDataSource(driver, jdbcDataSourceConfig.getUrl(), jdbcDataSourceConfig.getUsername(), jdbcDataSourceConfig.getPassword());
    }

    private <T> String parseSqlTemplateFile(FreemarkerConfigurationForSqlTemplates freemarkerConfiguration, String templateFile, T dataModel) throws IOException, TemplateException {
        Template sqlTemplate = freemarkerConfiguration.getTemplate(templateFile);
        String sqlText;
        try (StringWriter writer = new StringWriter(4096)) {
            sqlTemplate.process(dataModel, writer);
            sqlText = writer.toString();
        }
        return sqlText;
    }

    private JdbcDataSourceConfigProperties getDataSourceConfig(SorConfigProperties sorConfig) throws ExecutorConfigurationException {
        JdbcDataSourceConfigProperties dataSourceConfig = sgsConfigProperties.getConnections().getJdbc().get(sorConfig.getConnectionName());
        if (dataSourceConfig == null) {
            throw new ExecutorConfigurationException("No data source connection configuration with the connection-name of " + sorConfig.getConnectionName() + " is configured");
        }
        return dataSourceConfig;
    }
}
