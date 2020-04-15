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

import spock.lang.Specification

import static edu.berkeley.bidms.app.matchengine.config.MatchConfig.MatchType.DISTANCE
import static edu.berkeley.bidms.app.matchengine.config.MatchConfig.MatchType.EXACT
import static edu.berkeley.bidms.app.matchengine.config.MatchConfig.MatchType.SUBSTRING

class MatchConfigFactoryBeanSpec extends Specification {
    def "test that parseConfig will return a valid config"() {
        setup:
        def config = """
            import static edu.berkeley.bidms.app.matchengine.config.MatchConfig.MatchType.*

            matchTable('myTableName')

            referenceId {
                responseType = "enterprise"
            }

            attributes {
                'sor' {
                    description = "sor description"
                    column = "sorColumn"
                }
                'name' {
                    description = "name description"
                    column = "nameColumn"
                }
            }
            confidences {
                superCanonical sor: EXACT
                canonical "Custom Name", sor: EXACT
                canonical sor: EXACT, name: SUBSTRING
                potential sor: EXACT, name: DISTANCE
            }
        """

        when:
        def matchConfig = MatchConfigFactoryBean.parseConfig(config)

        then:
        matchConfig.matchTable == 'myTableName'
        matchConfig.matchReference.responseType == 'enterprise'
        matchConfig.matchAttributeConfigs.name == ['sor', 'name']
        matchConfig.superCanonicalConfidences*.confidence == [[sor: EXACT]]
        matchConfig.superCanonicalConfidences*.ruleName == ["SuperCanonical #1"]
        matchConfig.canonicalConfidences*.confidence == [[sor: EXACT], [sor: EXACT, name: SUBSTRING]]
        matchConfig.canonicalConfidences*.ruleName == ["Custom Name", "Canonical #2"]
        matchConfig.potentialConfidences*.confidence == [[sor: EXACT, name: DISTANCE]]
        matchConfig.potentialConfidences*.ruleName == ["Potential #1"]
    }
}
