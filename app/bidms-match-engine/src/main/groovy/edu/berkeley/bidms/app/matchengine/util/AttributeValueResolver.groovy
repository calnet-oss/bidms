/*
 * Copyright (c) 2014, Regents of the University of California and
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
package edu.berkeley.bidms.app.matchengine.util

import edu.berkeley.bidms.app.matchengine.config.MatchAttributeConfig

class AttributeValueResolver {
    private AttributeValueResolver() {}

    static String getAttributeValue(MatchAttributeConfig config, Map matchInput) {
        if (config?.search?.fixedValue) {
            return config.search.fixedValue
        }
        if (config.path) {
            // Find the candidates in the given path
            def candidates = matchInput[config.path] as List<Map>
            // If no candidates return null
            if (!candidates) {
                return null
            }
            // If the config has a group specified, find the element with type = "group" otherwise take the first element from candidates that
            // does not have a type, and finally try with the first one
            Map candidate = config.group ? candidates.find { it.type == config.group } : (candidates.find { !it.type } ?: candidates.first())

            return normalizeValue(config, candidate?.getAt(config.attribute))
        } else {
            return normalizeValue(config, matchInput.getAt(config.attribute))
        }
    }

    static List getAttributeValues(MatchAttributeConfig config, Map matchInput) {
        if (!config.path) {
            throw new IllegalStateException("path is a required configuration value when configuring list attributes")
        }
        List inputValues = matchInput[config.path] as List
        if (!inputValues) {
            return null
        }
        return inputValues
    }

    static List<String> getStringAttributeValues(MatchAttributeConfig config, Map matchInput) {
        List<String> inputValues = getAttributeValues(config, matchInput) as List<String>
        return inputValues.collect { value ->
            normalizeValue(config, value?.toString())
        }
    }

    private static String normalizeValue(MatchAttributeConfig matchAttributeConfig, String value) {
        // If the nullEquivalents is not set, return the value
        if (!matchAttributeConfig.nullEquivalents) {
            value
        }
        // Expect matchAttributeConfig ot be a list of Regular Expressions. If any of these matches, it's a null like value
        def nullMatches = matchAttributeConfig.nullEquivalents.any {
            value ==~ it
        }
        return nullMatches ? null : value
    }

}
