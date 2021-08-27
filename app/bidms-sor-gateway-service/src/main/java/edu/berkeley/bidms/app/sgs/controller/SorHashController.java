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
package edu.berkeley.bidms.app.sgs.controller;

import edu.berkeley.bidms.app.restservice.common.service.RestRequestRouterService;
import edu.berkeley.bidms.app.sgs.model.request.SorHashRequest;
import edu.berkeley.bidms.app.sgs.model.response.SorHashResponse;
import edu.berkeley.bidms.app.sgs.service.SorHashService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RequestMapping(value = "/sgs")
@RestController
public class SorHashController {

    private RestRequestRouterService routerService;
    private SorHashService sorHashService;

    public SorHashController(RestRequestRouterService routerService, SorHashService sorHashService) {
        this.routerService = routerService;
        this.sorHashService = sorHashService;
    }

    // curl http://localhost:8080/sgs/hash/SORNAME && echo
    @GetMapping(value = "/hash/{sorName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SorHashResponse hash(
            HttpServletRequest request,
            SorHashRequest cmd
    ) {
        return routerService.toService(request, cmd, () -> sorHashService.hash(cmd));
    }
}
