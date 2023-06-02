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

import jakarta.validation.constraints.NotNull;

public class SorConfigProperties {

    @NotNull
    private String sorName;

    @NotNull
    private String hashExecutorName;

    private boolean hashQueryTimestampSupported;

    private boolean queryTimestampSupported;

    @NotNull
    private String queryExecutorName;

    private String queryEntryExtractorName;

    private String connectionName;

    private JdbcConfigProperties jdbc;

    private LdapConfigProperties ldap;

    public String getSorName() {
        return sorName;
    }

    public void setSorName(String sorName) {
        this.sorName = sorName;
    }

    public String getHashExecutorName() {
        return hashExecutorName;
    }

    public void setHashExecutorName(String hashExecutorName) {
        this.hashExecutorName = hashExecutorName;
    }

    public boolean isHashQueryTimestampSupported() {
        return hashQueryTimestampSupported;
    }

    public void setHashQueryTimestampSupported(boolean hashQueryTimestampSupported) {
        this.hashQueryTimestampSupported = hashQueryTimestampSupported;
    }

    public boolean isQueryTimestampSupported() {
        return queryTimestampSupported;
    }

    public void setQueryTimestampSupported(boolean queryTimestampSupported) {
        this.queryTimestampSupported = queryTimestampSupported;
    }

    public String getQueryExecutorName() {
        return queryExecutorName;
    }

    public void setQueryExecutorName(String queryExecutorName) {
        this.queryExecutorName = queryExecutorName;
    }

    public String getQueryEntryExtractorName() {
        return queryEntryExtractorName;
    }

    public void setQueryEntryExtractorName(String queryEntryExtractorName) {
        this.queryEntryExtractorName = queryEntryExtractorName;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public JdbcConfigProperties getJdbc() {
        return jdbc;
    }

    public void setJdbc(JdbcConfigProperties jdbc) {
        this.jdbc = jdbc;
    }

    public LdapConfigProperties getLdap() {
        return ldap;
    }

    public void setLdap(LdapConfigProperties ldap) {
        this.ldap = ldap;
    }
}
