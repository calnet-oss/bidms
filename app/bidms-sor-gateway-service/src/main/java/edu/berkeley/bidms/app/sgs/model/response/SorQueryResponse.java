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
package edu.berkeley.bidms.app.sgs.model.response;

import jakarta.validation.constraints.NotNull;

/**
 * Contains information regarding the result of a query.
 */
public class SorQueryResponse {

    public enum QueryMode {FULL, INDIVIDUAL, LAST_CHANGED}

    @NotNull
    private String sorName;

    @NotNull
    private QueryMode queryMode;

    private int successfulQueryCount;

    private int failedQueryCount;

    private int deletedCount;

    public SorQueryResponse(@NotNull String sorName, @NotNull QueryMode queryMode) {
        this.sorName = sorName;
        this.queryMode = queryMode;
    }

    public SorQueryResponse(@NotNull String sorName, @NotNull QueryMode queryMode, int successfulQueryCount, int failedQueryCount, int deletedCount) {
        this.sorName = sorName;
        this.queryMode = queryMode;
        this.successfulQueryCount = successfulQueryCount;
        this.failedQueryCount = failedQueryCount;
        this.deletedCount = deletedCount;
    }

    public String getSorName() {
        return sorName;
    }

    public void setSorName(String sorName) {
        this.sorName = sorName;
    }

    public QueryMode getQueryMode() {
        return queryMode;
    }

    public void setQueryMode(QueryMode queryMode) {
        this.queryMode = queryMode;
    }

    public int getSuccessfulQueryCount() {
        return successfulQueryCount;
    }

    public void setSuccessfulQueryCount(int successfulQueryCount) {
        this.successfulQueryCount = successfulQueryCount;
    }

    public int getFailedQueryCount() {
        return failedQueryCount;
    }

    public void setFailedQueryCount(int failedQueryCount) {
        this.failedQueryCount = failedQueryCount;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(int deletedCount) {
        this.deletedCount = deletedCount;
    }
}
