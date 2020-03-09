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
package edu.berkeley.bidms.app.sgs.controller;

import edu.berkeley.bidms.app.restservice.common.service.RestRequestRouterService;
import edu.berkeley.bidms.app.sgs.model.request.SorQueryRequest;
import edu.berkeley.bidms.app.sgs.model.response.SorQueryResponse;
import edu.berkeley.bidms.app.sgs.service.SorQueryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(value = "Perform query operations on a SOR")
@RequestMapping(value = "/sgs")
@RestController
public class SorQueryController {

    private RestRequestRouterService routerService;
    private SorQueryService sorQueryService;

    public SorQueryController(RestRequestRouterService routerService, SorQueryService sorQueryService) {
        this.routerService = routerService;
        this.sorQueryService = sorQueryService;
    }

    // curl http://localhost:8080/sgs/query/SORNAME && echo
    @ApiOperation(value = "Perform a query on a SOR")
    @GetMapping(value = "/query/{sorName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SorQueryResponse query(
            HttpServletRequest request,
            @ApiParam(value = "Parameters for a request to query a SOR") SorQueryRequest cmd
    ) {
        return routerService.toService(request, cmd, () -> sorQueryService.query(cmd));
    }
}
