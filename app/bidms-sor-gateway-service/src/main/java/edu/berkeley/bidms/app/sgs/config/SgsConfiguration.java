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
package edu.berkeley.bidms.app.sgs.config;

import edu.berkeley.bidms.app.sgs.config.freemarker.FreemarkerConfigurationForSqlTemplates;
import edu.berkeley.bidms.app.sgs.config.properties.SgsConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

@Configuration
public class SgsConfiguration {
    private final Logger log = LoggerFactory.getLogger(SgsConfiguration.class);

    private SgsConfigProperties sorGatewayServiceConfigProperties;

    @Value("${spring.datasource.url}")
    private String dsUrl;

    @Value("${spring.datasource.username}")
    private String dsUsername;

    @Value("${spring.datasource.password}")
    private String dsPassword;

    public SgsConfiguration(SgsConfigProperties sorGatewayServiceConfigProperties) {
        this.sorGatewayServiceConfigProperties = sorGatewayServiceConfigProperties;
        // Oracle JDBC driver doesn't do the right thing with
        // java.sql.Timestamp objects unless we set this.
        System.getProperties().setProperty("oracle.jdbc.J2EE13Compliant", "true");
    }

    @Bean
    public FreemarkerConfigurationForSqlTemplates getFreemarkerConfigurationForSqlTemplates() throws IOException {
        FreemarkerConfigurationForSqlTemplates configuration = new FreemarkerConfigurationForSqlTemplates();
        URL url = new URL(sorGatewayServiceConfigProperties.getSqlTemplateDirectory());
        configuration.setDirectoryForTemplateLoading(new File(url.getFile()));
        return configuration;
    }

    public String getJdbcUrl() {
        return dsUrl;
    }

    public Properties getJdbcConnectionProperties() {
        Properties props = new Properties();
        props.put("user", dsUsername);
        props.put("password", dsPassword);
        return props;
    }
}
