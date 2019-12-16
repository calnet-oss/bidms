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
package edu.berkeley.bidms.app.hello.controller;

import edu.berkeley.bidms.app.hello.model.request.HelloWorldRequest;
import edu.berkeley.bidms.app.hello.model.response.HelloWorldResponse;
import edu.berkeley.bidms.app.hello.service.HelloWorldService;
import edu.berkeley.bidms.app.restservice.common.service.RestRequestRouterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(value = "Hello World")
@RequestMapping(value = "/hello")
@RestController
public class HelloWorldController {

    private RestRequestRouterService routerService;
    private HelloWorldService helloWorldService;

    public HelloWorldController(RestRequestRouterService routerService, HelloWorldService helloWorldService) {
        this.routerService = routerService;
        this.helloWorldService = helloWorldService;
    }

    // curl http://localhost:8080/hello/hello?name=me && echo
    @SuppressWarnings("unchecked")
    @ApiOperation(value = "View default hello world response")
    @GetMapping(value = "/hello", produces = "application/json")
    public HelloWorldResponse helloGet(
            HttpServletRequest request,
            @ApiParam(value = "A HelloWorld request that contains a name")
            @ModelAttribute HelloWorldRequest cmd) {
        return routerService.toService(request, cmd, () -> helloWorldService.message(cmd));
    }

    // curl -X POST http://localhost:8080/hello/hello --header "Content-Type: application/json" -d '{"name": "me"}' && echo
    @SuppressWarnings("unchecked")
    @ApiOperation(value = "View an addressed hello world response")
    @PostMapping(value = "/hello", consumes = "application/json", produces = "application/json")
    public HelloWorldResponse helloPost(
            HttpServletRequest request,
            @ApiParam(value = "A HelloWorld request that contains a name")
            @RequestBody HelloWorldRequest cmd
    ) {
        return routerService.toService(request, cmd, () -> helloWorldService.message(cmd));
    }
}
