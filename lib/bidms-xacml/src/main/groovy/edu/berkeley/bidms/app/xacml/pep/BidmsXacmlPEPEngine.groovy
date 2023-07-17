/*
 * Copyright (c) 2023, Regents of the University of California and
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
package edu.berkeley.bidms.app.xacml.pep

import com.att.research.xacml.api.Request
import com.att.research.xacml.api.Response
import com.att.research.xacml.api.pdp.PDPEngine
import com.att.research.xacml.api.pdp.PDPException
import com.att.research.xacml.api.pep.PEPEngine
import com.att.research.xacml.api.pep.PEPException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Slf4j
@Component
@CompileStatic
class BidmsXacmlPEPEngine implements PEPEngine {

    final PDPEngine pdpEngine

    BidmsXacmlPEPEngine(PDPEngine engine) {
        this.pdpEngine = engine
    }

    @Override
    Response decide(Request pepRequest) throws PEPException {
        Response response = null;
        try {
            response = pdpEngine.decide(pepRequest)
        } catch (PDPException ex) {
            throw new PEPException("PDPException deciding on Request", ex)
        }
        log.debug("Decided request {} to response {}", pepRequest, response)
        return response
    }
}
