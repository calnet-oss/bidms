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
package edu.berkeley.bidms.app.xacml.policy

import edu.berkeley.bidms.xacml.pdp.BidmsXacmlPolicyFinder
import edu.berkeley.bidms.xacml.pdp.PersonSubjectResourceFunctionPolicy
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@Component
@CompileStatic
class MatchingDepartmentPolicy extends PersonSubjectResourceFunctionPolicy {

    static final String POLICY_ID = "urn:edu:berkeley:bidms:xacml:policy:matching-department-policy"
    static final String DESCRIPTION = "Matching department policy"

    MatchingDepartmentPolicy(BidmsXacmlPolicyFinder policyFinder) {
        super(
                policyFinder,
                POLICY_ID,
                DESCRIPTION,
                "person-edit",
                MatchingDepartmentRule.RULE_ID,
                MatchingDepartmentRule.name
        )
    }
}
