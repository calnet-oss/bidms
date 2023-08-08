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
package edu.berkeley.bidms.app.xacml.pdp

import com.att.research.xacml.api.Identifier
import com.att.research.xacml.api.XACML3
import com.att.research.xacml.std.IdentifierImpl
import com.att.research.xacml.std.StdStatus
import com.att.research.xacml.std.StdStatusCode
import com.att.research.xacml.std.datatypes.DataTypeBoolean
import com.att.research.xacmlatt.pdp.eval.EvaluationContext
import com.att.research.xacmlatt.pdp.policy.ExpressionResult
import com.att.research.xacmlatt.pdp.policy.FunctionArgument
import com.att.research.xacmlatt.pdp.policy.FunctionDefinition
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.xacml.pdp.BidmsXacmlFunctionDefinitionFactory
import edu.berkeley.bidms.xacml.pdp.PersonSubjectResourceEvaluator
import edu.berkeley.bidms.xacml.request.PersonSubjectResourceRequest
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

import jakarta.annotation.PostConstruct

@Component
@CompileStatic
class PersonSubjectResourceFunctionDefinition implements FunctionDefinition {
    public static final Identifier FD_GROOVY = new IdentifierImpl("urn:edu:calnet:xacml:function:spring-subject-resource-evaluator")

    @PostConstruct
    void init() {
        BidmsXacmlFunctionDefinitionFactory.register(this)
    }

    @Override
    Identifier getId() {
        return FD_GROOVY
    }

    @Override
    Identifier getDataTypeId() {
        return XACML3.ID_DATATYPE_STRING
    }

    @Override
    boolean returnsBag() {
        return false
    }

    @Override
    ExpressionResult evaluate(EvaluationContext evaluationContext, List<FunctionArgument> arguments) {
        if (arguments == null || arguments.size() != 1) {
            return ExpressionResult.newError(new StdStatus(StdStatusCode.STATUS_CODE_PROCESSING_ERROR,
                    "Expected 1 argument, which should be the spring bean class name.  Instead, got ${arguments?.size()} arguments."
            ))
        }

        FunctionArgument springBeanClassName = arguments.get(0)
        if (springBeanClassName.isBag()) {
            return ExpressionResult.newError(new StdStatus(StdStatusCode.STATUS_CODE_PROCESSING_ERROR, "Not expecting a bag for argument 0."))
        }
        if (springBeanClassName.getValue().getDataTypeId() != XACML3.ID_DATATYPE_STRING) {
            return ExpressionResult.newError(new StdStatus(StdStatusCode.STATUS_CODE_PROCESSING_ERROR, "Expected a String for argument 0."));
        }

        Person subject = null
        List<String> subjectRoles = null
        Person resource = null
        evaluationContext.request.requestAttributes.each { ra ->
            ra.attributes.each { attr ->
                if (attr.attributeId.stringValue() == "urn:oasis:names:tc:xacml:1.0:subject:subject-id") {
                    subject = (Person) attr.values.first().value
                } else if (attr.attributeId.stringValue() == PersonSubjectResourceRequest.SUBJECT_ROLES_ATTRIBUTE_ID) {
                    subjectRoles = (List<String>) attr.values.collect { it.value as String }
                } else if (attr.attributeId.stringValue() == "urn:oasis:names:tc:xacml:1.0:resource:resource-id") {
                    resource = (Person) attr.values.first().value
                }
            }
        }

        Class<? extends PersonSubjectResourceEvaluator> theClass = (Class<? extends PersonSubjectResourceEvaluator>) Class.forName((String) springBeanClassName.value.value)
        PersonSubjectResourceEvaluator instance = theClass.getDeclaredConstructor().newInstance()
        return ExpressionResult.newSingle(DataTypeBoolean.newInstance().createAttributeValue(
                instance.evaluate(subject, subjectRoles, resource)
        ))
    }
}
