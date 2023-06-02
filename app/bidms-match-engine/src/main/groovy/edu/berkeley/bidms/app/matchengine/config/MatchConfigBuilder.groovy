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
package edu.berkeley.bidms.app.matchengine.config

import groovy.transform.CompileStatic

import static MatchConfig.MatchType

@CompileStatic
class MatchConfigBuilder {

    MatchConfig config = new MatchConfig()

    void matchTable(String tableName) {
        config.matchTable = tableName
    }

    void referenceId(Closure closure) {
        MatchReference matchReference = new MatchReference()
        closure.delegate = matchReference
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call()
        config.matchReference = matchReference
    }

    void attributes(Closure closure) {
        MatchAttributesDelegate builder = new MatchAttributesDelegate()
        closure.delegate = builder
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call()
        config.matchAttributeConfigs = builder.matchAttributes
    }

    void confidences(Closure closure) {
        MatchConfidencesDelegate builder = new MatchConfidencesDelegate(config.matchAttributeConfigs)
        closure.delegate = builder
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call()
        config.superCanonicalConfidences = builder.superCanonicalConfidences
        config.canonicalConfidences = builder.canonicalConfidences
        config.potentialConfidences = builder.potentialConfidences
    }

    // Builds a list of matchAttributeConfigs by dynamically invoking each method in the closure 'attributes'
    @CompileStatic
    private class MatchAttributesDelegate {
        List<MatchAttributeConfig> matchAttributes = []

        @SuppressWarnings("GroovyAssignabilityCheck")
        def methodMissing(String name, Object args) {
            assert ((Collection) args).size() == 1
            assert ((Collection) args)[0] instanceof Closure

            MatchAttributeDelegate builder = new MatchAttributeDelegate(name)

            def closure = ((Collection) args)[0] as Closure
            closure.delegate = builder
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()

            matchAttributes << builder.matchAttribute
        }
    }

    // Creates and sets properties on a matchAttribute by invoking setProperty and handles the search and input closures
    @CompileStatic
    private class MatchAttributeDelegate {
        final MatchAttributeConfig matchAttribute

        MatchAttributeDelegate(String name) {
            this.matchAttribute = new MatchAttributeConfig(name: name)
        }

        void setProperty(String name, Object value) {
            if (matchAttribute.hasProperty(name)) {
                matchAttribute.getClass().getMethods().find {
                    it.name == "set" + name[0].toUpperCase() + name.substring(1)
                }.invoke(matchAttribute, value)
            } else {
                throw new MissingPropertyException(name, MatchAttributeConfig)
            }
        }

        /**
         * assigns a SearchSettings as the delegate and executes the search closure
         */
        void search(Closure closure) {
            def search = new MatchAttributeConfig.SearchSettings()
            closure.delegate = search
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()
            matchAttribute.search = search
        }

        /**
         * assigns a InputSettings as the delegate and executes the input closure
         */
        void input(Closure closure) {
            def input = new MatchAttributeConfig.InputSettings()
            closure.delegate = input
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()
            matchAttribute.input = input
        }
    }

    // Creates and set canonical and potential confidences, validating that the attributeNames are present
    @CompileStatic
    private class MatchConfidencesDelegate {

        private List<String> matchAttributeNames
        List<MatchConfidence> superCanonicalConfidences = []
        List<MatchConfidence> canonicalConfidences = []
        List<MatchConfidence> potentialConfidences = []

        MatchConfidencesDelegate(List<MatchAttributeConfig> matchAttributes) {
            assert matchAttributes
            this.matchAttributeNames = matchAttributes.name
        }

        void superCanonical(Map<String, MatchType> superCanonical, String rule = null) {
            validateKeysAndValues("superCanonical", superCanonical, MatchType.SUPERCANONICAL_TYPES)
            rule = rule ?: "SuperCanonical #${superCanonicalConfidences.size() + 1}"
            superCanonicalConfidences << new MatchConfidence(ruleName: rule, confidence: superCanonical)
        }

        void canonical(Map<String, MatchType> canonical, String rule = null) {
            validateKeysAndValues("canonical", canonical, MatchType.CANONICAL_TYPES)
            rule = rule ?: "Canonical #${canonicalConfidences.size() + 1}"
            canonicalConfidences << new MatchConfidence(ruleName: rule, confidence: canonical)
        }

        void potential(Map<String, MatchType> potential, String rule = null) {
            validateKeysAndValues("potential", potential, MatchType.POTENTIAL_TYPES)
            rule = rule ?: "Potential #${potentialConfidences.size() + 1}"

            potentialConfidences << new MatchConfidence(ruleName: rule, confidence: potential)
        }

        private void validateKeysAndValues(String type, Map<String, MatchType> set, List validTypes) {
            def wrongKeys = set.findAll { !(it.key in matchAttributeNames) }
            if (wrongKeys) {
                throw new RuntimeException("Keys in: $type ${set.collect { "$it.key: $it.value" }.join(', ')} is mismatching on the following keys: ${wrongKeys*.key}")
            }
            assert set.values().every { it in validTypes }
        }
    }
}
