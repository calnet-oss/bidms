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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

/**
 * A service to handle an uncaught exception when synchronously routing to a
 * bean method.
 */
@Service
public class UnexpectedExceptionService {
    private final Logger log = LoggerFactory.getLogger(UnexpectedExceptionService.class);

    /**
     * Handles a runtime exception that resulted from synchronously routing
     * to a bean method.
     * <p>
     * There are two types of runtime exceptions that may be thrown by the
     * bean method: expected and unexpected.
     * <p>
     * It's considered an expected exception when the exception is already an
     * instance of {@link ResponseStatusException}.  By throwing that, the
     * programmer has already handled the error and decided what the HTTP
     * response should be.  In this case, the exception is not logged and the
     * {@link ResponseStatusException} is rethrown as-is.
     * <p>
     * Otherwise, it's considered an unexpected exception and it's logged as
     * an error then wrapped in a {@link ServerErrorException} which is then
     * thrown.
     * <p>
     * In the context of a web controller, Spring will respond with the http
     * code associated with the {@link ResponseStatusException}.
     *
     * @param request   Optionally, the request body.
     * @param exception The exception that was thrown by the bean method.
     * @param <R>       The class type for the request body.
     * @throws ResponseStatusException In the context of a web controller,
     *                                 Spring will respond with the http code
     *                                 associated with the {@link
     *                                 ResponseStatusException}
     */
    public <R> void respond(R request, Exception exception) throws ResponseStatusException {
        if (exception instanceof ResponseStatusException) {
            // expected exception - rethrow as-is without extra logging
            throw (ResponseStatusException) exception;
        } else {
            // unexpected exception - log and wrap as a ServerErrorException (http code 500)
            log.error("Unexpected server error", exception);
            throw new ServerErrorException("Unexpected server error", exception);
        }
    }
}
