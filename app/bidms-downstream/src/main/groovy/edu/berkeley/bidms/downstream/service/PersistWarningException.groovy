/*
 * Copyright (c) 2025, Regents of the University of California and
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
package edu.berkeley.bidms.downstream.service

import groovy.transform.InheritConstructors

/**
 * Can be used by an implementation of
 * {@link edu.berkeley.bidms.app.downstream.service.BaseUidQueueConsumerService}
 * to indicate that an uid could not be persisted downstream but that it
 * should be logged as a warning rather than an error.  You may want to do
 * this if the failure is detected as a temporary failure state that is
 * expected to be rectified on its own later.  The primary benefit of this
 * is to log the warning but keep the full stack trace out of the log.
 */
@InheritConstructors
class PersistWarningException extends PersistException {
    /**
     * If an exception causation chain contains a {@link
     * PersistWarningException}, this will return the first occurrence of it
     * in the chain.
     *
     * @param e The exception with a possible causation chain to search through.
     *
     * @return The first occurrence of {@link PersistWarningException}
     *         in the chain, otherwise null.
     */
    static PersistWarningException findPersistWarningExceptionInChain(Exception e) {
        if (e instanceof PersistWarningException) {
            return (PersistWarningException) e
        }
        return e.cause instanceof Exception ? findPersistWarningExceptionInChain((Exception) e.cause) : null
    }
}
