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

import com.att.research.xacml.api.Identifier
import com.att.research.xacmlatt.pdp.policy.FunctionDefinition
import com.att.research.xacmlatt.pdp.policy.FunctionDefinitionFactory
import com.att.research.xacmlatt.pdp.std.StdFunctionDefinitionFactory
import groovy.transform.CompileStatic

@CompileStatic
class BidmsXacmlFunctionDefinitionFactory extends FunctionDefinitionFactory {

    private final static Map<Identifier, FunctionDefinition> mapFunctionDefinitions = [:]
    private final FunctionDefinitionFactory stdFunctionDefinitionFactory = new StdFunctionDefinitionFactory()

    static void register(FunctionDefinition functionDefinition) {
        mapFunctionDefinitions.put(functionDefinition.getId(), functionDefinition);
    }

    @Override
    FunctionDefinition getFunctionDefinition(Identifier functionId) {
        FunctionDefinition functionDefinition = mapFunctionDefinitions.get(functionId)
        return functionDefinition != null ? functionDefinition : stdFunctionDefinitionFactory.getFunctionDefinition(functionId)
    }
}