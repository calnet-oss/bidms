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

import javax.validation.constraints.NotNull;

/**
 * Contains information regarding the result of a hash query.
 */
public class SorHashResponse {

    public enum HashMode { FULL, LAST_CHANGED }

    @NotNull
    private String sorName;

    @NotNull
    private HashMode hashMode;

    private int successfulHashCount;

    private int failedHashCount;

    public SorHashResponse(@NotNull String sorName, @NotNull HashMode hashMode, int successCount, int failureCount) {
        this.sorName = sorName;
        this.hashMode = hashMode;
        this.successfulHashCount = successCount;
        this.failedHashCount = failureCount;
    }

    public String getSorName() {
        return sorName;
    }

    public void setSorName(String sorName) {
        this.sorName = sorName;
    }

    public HashMode getHashMode() {
        return hashMode;
    }

    public void setHashMode(HashMode hashMode) {
        this.hashMode = hashMode;
    }

    public int getSuccessfulHashCount() {
        return successfulHashCount;
    }

    public void setSuccessfulHashCount(int successfulHashCount) {
        this.successfulHashCount = successfulHashCount;
    }

    public int getFailedHashCount() {
        return failedHashCount;
    }

    public void setFailedHashCount(int failedHashCount) {
        this.failedHashCount = failedHashCount;
    }
}
