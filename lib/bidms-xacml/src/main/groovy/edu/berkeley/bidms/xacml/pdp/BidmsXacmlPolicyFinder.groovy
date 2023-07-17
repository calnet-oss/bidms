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

import com.att.research.xacml.api.IdReferenceMatch
import com.att.research.xacml.api.Identifier
import com.att.research.xacmlatt.pdp.eval.EvaluationContext
import com.att.research.xacmlatt.pdp.policy.Policy
import com.att.research.xacmlatt.pdp.policy.PolicyDef
import com.att.research.xacmlatt.pdp.policy.PolicyFinder
import com.att.research.xacmlatt.pdp.policy.PolicyFinderResult
import com.att.research.xacmlatt.pdp.policy.PolicySet
import com.att.research.xacmlatt.pdp.std.StdPolicyFinder
import com.att.research.xacmlatt.pdp.std.StdPolicyFinderResult
import groovy.transform.CompileStatic

@CompileStatic
class BidmsXacmlPolicyFinder implements PolicyFinder {
    final PolicyFinder parent

    private static final Map<Identifier, PolicyDef> rootPolicyDefMap = [:]

    BidmsXacmlPolicyFinder(PolicyFinder parent) {
        this.parent = parent
    }

    @Override
    PolicyFinderResult<PolicyDef> getRootPolicyDef(EvaluationContext evaluationContext) {
        def policyDefFirstMatch = rootPolicyDefMap.find { k, v ->
            v.match(evaluationContext).status.ok
        }
        def retVal = policyDefFirstMatch ? new StdPolicyFinderResult<>(policyDefFirstMatch.value) : parent.getRootPolicyDef(evaluationContext)
        return retVal
    }

    @Override
    PolicyFinderResult<Policy> getPolicy(IdReferenceMatch idReferenceMatch) {
        return parent.getPolicy(idReferenceMatch)
    }

    @Override
    PolicyFinderResult<PolicySet> getPolicySet(IdReferenceMatch idReferenceMatch) {
        return parent.getPolicySet(idReferenceMatch)
    }

    @Override
    void shutdown() {
        parent.shutdown()
    }

    void addRootPolicyDef(PolicyDef policyDef) {
        ((StdPolicyFinder) parent).addReferencedPolicy(policyDef)
        rootPolicyDefMap[policyDef.identifier] = policyDef
    }
}
