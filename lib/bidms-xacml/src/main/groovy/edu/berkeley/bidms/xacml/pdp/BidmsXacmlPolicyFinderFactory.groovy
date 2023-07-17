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
package edu.berkeley.bidms.xacml.pdp

import com.att.research.xacml.util.FactoryException
import com.att.research.xacmlatt.pdp.policy.PolicyFinder
import com.att.research.xacmlatt.pdp.policy.PolicyFinderFactory
import com.att.research.xacmlatt.pdp.std.StdPolicyFinderFactory
import groovy.transform.CompileStatic
import groovy.transform.Synchronized

@CompileStatic
class BidmsXacmlPolicyFinderFactory extends PolicyFinderFactory {

    private final PolicyFinderFactory stdPolicyFinderFactory = new StdPolicyFinderFactory()

    private volatile PolicyFinder defaultPolicyFinder
    private volatile PolicyFinder policyFinderWithProperties

    BidmsXacmlPolicyFinderFactory() {
        super()
    }

    BidmsXacmlPolicyFinderFactory(Properties properties) {
        super(properties)
    }

    @Synchronized
    @Override
    PolicyFinder getPolicyFinder() throws FactoryException {
        if (!defaultPolicyFinder) {
            this.defaultPolicyFinder = new BidmsXacmlPolicyFinder(stdPolicyFinderFactory.policyFinder)
        }
        return defaultPolicyFinder
    }

    @Synchronized
    @Override
    PolicyFinder getPolicyFinder(Properties properties) throws FactoryException {
        if (!policyFinderWithProperties) {
            this.policyFinderWithProperties = new BidmsXacmlPolicyFinder(stdPolicyFinderFactory.getPolicyFinder(properties))
        }
        return policyFinderWithProperties
    }
}
