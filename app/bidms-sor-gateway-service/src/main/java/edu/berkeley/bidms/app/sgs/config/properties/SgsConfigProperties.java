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
package edu.berkeley.bidms.app.sgs.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "bidms.sgs")
public class SgsConfigProperties {

    @NotNull
    private String sqlTemplateDirectory;

    private Map<String, SorConfigProperties> sors;

    private Map<String, HashExecutorConfigProperties> hashExecutors;

    private Map<String, QueryExecutorConfigProperties> queryExecutors;

    private Map<String, QueryEntryExtractorConfigProperties> queryEntryExtractors;

    private ConnectionConfigProperties connections;

    private Map<String, DirectoryAttributeMetadataConfigProperties> directoryAttributeMetadata;

    public String getSqlTemplateDirectory() {
        return sqlTemplateDirectory;
    }

    public void setSqlTemplateDirectory(String sqlTemplateDirectory) {
        this.sqlTemplateDirectory = sqlTemplateDirectory;
    }

    public Map<String, SorConfigProperties> getSors() {
        return sors;
    }

    public void setSors(Map<String, SorConfigProperties> sors) {
        for (var entry : sors.entrySet()) {
            entry.getValue().setSorName(entry.getKey());
        }
        this.sors = sors;
    }

    public Map<String, HashExecutorConfigProperties> getHashExecutors() {
        return hashExecutors;
    }

    public void setHashExecutors(Map<String, HashExecutorConfigProperties> hashExecutors) {
        for (var entry : hashExecutors.entrySet()) {
            entry.getValue().setName(entry.getKey());
        }
        this.hashExecutors = hashExecutors;
    }

    public Map<String, QueryExecutorConfigProperties> getQueryExecutors() {
        return queryExecutors;
    }

    public void setQueryExecutors(Map<String, QueryExecutorConfigProperties> queryExecutors) {
        for (var entry : queryExecutors.entrySet()) {
            entry.getValue().setName(entry.getKey());
        }
        this.queryExecutors = queryExecutors;
    }

    public Map<String, QueryEntryExtractorConfigProperties> getQueryEntryExtractors() {
        return queryEntryExtractors;
    }

    public void setQueryEntryExtractors(Map<String, QueryEntryExtractorConfigProperties> queryEntryExtractors) {
        for (var entry : queryEntryExtractors.entrySet()) {
            entry.getValue().setName(entry.getKey());
        }
        this.queryEntryExtractors = queryEntryExtractors;
    }

    public ConnectionConfigProperties getConnections() {
        return connections;
    }

    public void setConnections(ConnectionConfigProperties connections) {
        this.connections = connections;
    }

    public Map<String, DirectoryAttributeMetadataConfigProperties> getDirectoryAttributeMetadata() {
        return directoryAttributeMetadata;
    }

    public void setDirectoryAttributeMetadata(Map<String, DirectoryAttributeMetadataConfigProperties> directoryAttributeMetadata) {
        for (var entry : directoryAttributeMetadata.entrySet()) {
            entry.getValue().setMetadataSetName(entry.getKey());
        }
        this.directoryAttributeMetadata = directoryAttributeMetadata;
    }
}
