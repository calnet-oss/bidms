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
package edu.berkeley.bidms.app.common.config.properties.rest;

import edu.berkeley.bidms.app.common.config.properties.rest.endpoint.RestDownstreamProvisionProperties;
import edu.berkeley.bidms.app.common.config.properties.rest.endpoint.RestMatchEngineProperties;
import edu.berkeley.bidms.app.common.config.properties.rest.endpoint.RestMatchServiceProperties;
import edu.berkeley.bidms.app.common.config.properties.rest.endpoint.RestProvisionProperties;
import jakarta.validation.constraints.NotNull;

public class RestProperties {

    @NotNull
    private RestMatchEngineProperties matchengine;
    @NotNull
    private RestProvisionProperties provision;
    @NotNull
    private RestMatchServiceProperties matchService;
    @NotNull
    private RestDownstreamProvisionProperties downstream;

    public RestMatchEngineProperties getMatchengine() {
        return matchengine;
    }

    public void setMatchengine(RestMatchEngineProperties matchengine) {
        this.matchengine = matchengine;
    }

    public RestProvisionProperties getProvision() {
        return provision;
    }

    public void setProvision(RestProvisionProperties provision) {
        this.provision = provision;
    }

    public RestMatchServiceProperties getMatchService() {
        return matchService;
    }

    public void setMatchService(RestMatchServiceProperties matchService) {
        this.matchService = matchService;
    }

    public RestDownstreamProvisionProperties getDownstream() {
        return downstream;
    }

    public void setDownstream(RestDownstreamProvisionProperties downstream) {
        this.downstream = downstream;
    }
}
