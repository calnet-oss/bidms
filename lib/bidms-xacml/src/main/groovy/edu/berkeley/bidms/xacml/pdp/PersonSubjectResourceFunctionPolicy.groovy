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

import com.att.research.xacml.std.IdentifierImpl
import com.att.research.xacml.std.StdAttributeValue
import com.att.research.xacml.std.StdVersion
import com.att.research.xacmlatt.pdp.policy.AllOf
import com.att.research.xacmlatt.pdp.policy.AnyOf
import com.att.research.xacmlatt.pdp.policy.Condition
import com.att.research.xacmlatt.pdp.policy.Match
import com.att.research.xacmlatt.pdp.policy.Policy
import com.att.research.xacmlatt.pdp.policy.Rule
import com.att.research.xacmlatt.pdp.policy.RuleEffect
import com.att.research.xacmlatt.pdp.policy.Target
import com.att.research.xacmlatt.pdp.policy.expressions.Apply
import com.att.research.xacmlatt.pdp.policy.expressions.AttributeDesignator
import com.att.research.xacmlatt.pdp.policy.expressions.AttributeValueExpression
import com.att.research.xacmlatt.pdp.std.StdCombiningAlgorithms
import edu.berkeley.bidms.app.xacml.pdp.PersonSubjectResourceFunctionDefinition
import groovy.transform.CompileStatic
import jakarta.annotation.PostConstruct

@CompileStatic
abstract class PersonSubjectResourceFunctionPolicy extends Policy {

    private BidmsXacmlPolicyFinder policyFinder

    PersonSubjectResourceFunctionPolicy(
            BidmsXacmlPolicyFinder policyFinder,
            String policyId,
            String description,
            String actionId,
            String ruleId,
            String subjectResourcePolicyFunctionClass
    ) {
        this.policyFinder = policyFinder
        this.identifier = new IdentifierImpl(policyId)
        this.description = description
        this.target = new Target().with {
            addAnyOf(new AnyOf([
                    new AllOf(
                            matches: [
                                    new Match().with {
                                        matchId = new IdentifierImpl("urn:oasis:names:tc:xacml:1.0:function:string-equal")
                                        attributeValue = new StdAttributeValue<String>(new IdentifierImpl("http://www.w3.org/2001/XMLSchema#string"), actionId)
                                        attributeRetrievalBase = new AttributeDesignator().with {
                                            category = new IdentifierImpl("urn:oasis:names:tc:xacml:3.0:attribute-category:action")
                                            dataTypeId = new IdentifierImpl("http://www.w3.org/2001/XMLSchema#string")
                                            mustBePresent = true
                                            attributeId = new IdentifierImpl("urn:oasis:names:tc:xacml:1.0:action:action-id")
                                            it
                                        }
                                        it
                                    }
                            ] as Collection<Match>
                    )
            ] as Collection<AllOf>))
            it
        }
        this.version = new StdVersion([1] as int[])
        addRule(getRule(this, ruleId, subjectResourcePolicyFunctionClass))
        this.ruleCombiningAlgorithm = StdCombiningAlgorithms.CA_RULE_DENY_UNLESS_PERMIT
    }

    @PostConstruct
    void init() {
        policyFinder.addRootPolicyDef(this)
    }

    private static Rule getRule(Policy policy, String ruleId, String subjectResourcePolicyFunctionClass) {
        return new Rule().with {
            it.policy = policy
            it.ruleId = ruleId
            ruleEffect = RuleEffect.PERMIT
            condition = new Condition().with {
                expression = new Apply().with {
                    functionId = new IdentifierImpl("urn:oasis:names:tc:xacml:1.0:function:boolean-equal")
                    addArgument(new Apply().with {
                        functionId = PersonSubjectResourceFunctionDefinition.FD_GROOVY
                        addArgument(new AttributeValueExpression(new StdAttributeValue<String>(new IdentifierImpl("http://www.w3.org/2001/XMLSchema#string"), subjectResourcePolicyFunctionClass)))
                        it
                    })
                    addArgument(new AttributeValueExpression(new StdAttributeValue<Boolean>(new IdentifierImpl("http://www.w3.org/2001/XMLSchema#boolean"), true)))
                    it
                }
                it
            }
            it
        }
    }
}
