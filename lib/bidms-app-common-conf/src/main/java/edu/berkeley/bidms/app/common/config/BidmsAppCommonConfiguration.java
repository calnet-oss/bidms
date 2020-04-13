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
package edu.berkeley.bidms.app.common.config;

import edu.berkeley.bidms.app.common.config.properties.BidmsConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@SpringBootConfiguration
public class BidmsAppCommonConfiguration {
    private final Logger log = LoggerFactory.getLogger(BidmsAppCommonConfiguration.class);

    @Value("${spring.datasource.driver-class-name}")
    private String dsDriverClassName;

    @Value("${spring.datasource.url}")
    private String dsUrl;

    @Value("${spring.datasource.username}")
    private String dsUsername;

    @Value("${spring.datasource.password}")
    private String dsPassword;

    private BidmsConfigProperties bidmsConfigProperties;

    public BidmsAppCommonConfiguration(BidmsConfigProperties bidmsConfigProperties) {
        this.bidmsConfigProperties = bidmsConfigProperties;
    }

    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder<? extends DataSource> dataSourceBuilder = DataSourceBuilder.create();
        DataSource dataSource = dataSourceBuilder
                .driverClassName(dsDriverClassName)
                .url(dsUrl)
                .username(dsUsername)
                .password(dsPassword)
                .build();
        log.debug("Using dataSource of type " + dataSource.getClass().getName());
        if (dataSource.getClass().isAssignableFrom(com.zaxxer.hikari.HikariDataSource.class)) {
            com.zaxxer.hikari.HikariDataSource ds = (com.zaxxer.hikari.HikariDataSource) dataSource;
            log.debug("dataSource configured with autoCommit=" + ds.isAutoCommit() + ", transactionIsolation=" + ds.getTransactionIsolation() + ", connectionTimeout=" + ds.getConnectionTimeout() + ", idleTimeout=" + ds.getIdleTimeout() + ", initializationFailTimeout=" + ds.getInitializationFailTimeout() + ", leakDetectionThreshold=" + ds.getLeakDetectionThreshold() + ", maximumPoolSize=" + ds.getMaximumPoolSize() + ", maxLifetime=" + ds.getMaxLifetime() + ", minimumIdle=" + ds.getMinimumIdle() + ", validationTimeout=" + ds.getValidationTimeout() + ", isIsolateInternalQueries=" + ds.isIsolateInternalQueries() + ", connectionInitSql=" + ds.getConnectionInitSql() + ", connectionTestQuery=" + ds.getConnectionTestQuery());
        }
        return dataSource;
    }

    @Bean
    public JdbcTemplate getRegistryJdbcTemplate() {
        return new JdbcTemplate(getDataSource());
    }
}
