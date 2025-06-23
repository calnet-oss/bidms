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
package edu.berkeley.bidms.app.restservice.common.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.Callable;

/**
 * Route controller requests to services.
 */
@Service
public class RestRequestRouterService {

    private UnexpectedExceptionService unexpectedExceptionService;

    /**
     * Constructor for bean injection.
     *
     * @param unexpectedExceptionService A {@link UnexpectedExceptionService}
     *                                   bean instance.
     */
    public RestRequestRouterService(UnexpectedExceptionService unexpectedExceptionService) {
        this.unexpectedExceptionService = unexpectedExceptionService;
    }

    /**
     * Controllers call this method when they receive a request.  This
     * delivers the request to the service by invoking {@code
     * serviceCallable.call()}, which the implementation can be passed in as
     * a lambda.  For example, from the controller:
     *
     * <pre>{@code
     * public String controllerMethod(HttpServletRequest request, RequestModel cmd) {
     *     return routerService.toService(request, cmd, ()->service.serviceMethod(cmd));
     * }
     * }</pre>
     * <p>
     * The purpose of this "middle-man" is to do boilerplate things like
     * handle unexpected exceptions and write to audit logs.
     *
     * @param servletRequest  The {@link HttpServletRequest} object passed in
     *                        from the controller.
     * @param cmd             The model instance containing request data.
     *                        This is typically a result of Spring data
     *                        binding in the controller.
     * @param serviceCallable A {@link Callable} object that can be passed in
     *                        as a lambda that makes the call to the
     *                        service.
     * @return The model instance containing the response data.  This is what
     * is returned by the {@code serviceCallable.call()} method  (which
     * should also be what is returned by the service bean method).
     */
    public <V, R> V toService(HttpServletRequest servletRequest, R cmd, Callable<V> serviceCallable) {
        try {
            return serviceCallable.call();
        } catch (Exception e) {
            // expected that this throws an exception
            unexpectedExceptionService.respond(servletRequest, e);
        }
        // should not get here
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "unexpected state");
    }
}
