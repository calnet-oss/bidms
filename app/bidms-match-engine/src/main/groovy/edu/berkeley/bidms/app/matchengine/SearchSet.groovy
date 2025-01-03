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
package edu.berkeley.bidms.app.matchengine

import edu.berkeley.bidms.app.matchengine.config.MatchAttributeConfig
import edu.berkeley.bidms.app.matchengine.config.MatchConfidence
import edu.berkeley.bidms.app.matchengine.config.MatchConfig
import edu.berkeley.bidms.app.matchengine.util.AttributeValueResolver
import edu.berkeley.bidms.app.matchengine.util.sql.SqlWhereResolver
import edu.berkeley.bidms.app.matchengine.util.sql.WhereAndValues
import edu.berkeley.bidms.app.matchengine.util.sql.WhereValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SearchSet {
    private final Logger log = LoggerFactory.getLogger(SearchSet.class);

    MatchConfidence matchConfidence;
    List<MatchAttributeConfig> matchAttributeConfigs;

    WhereAndValues buildWhereClause(Map matchInput) {
        List<WhereValue> whereAndValues = matchConfidence.confidence.collect { String name, MatchConfig.MatchType matchType ->
            def config = matchAttributeConfigs.find { it.name == name }
            if (config.input?.fixedValue && matchInput[config.attribute] != config.input.fixedValue) {
                // matchInput attribute value does not match the fixedValue
                // in the 'input' part of the config
                return null
            }
            if (config.input?.list || config.input?.stringList) {
                def values = config.input?.stringList ? AttributeValueResolver.getStringAttributeValues(config, matchInput) : AttributeValueResolver.getAttributeValues(config, matchInput)
                if (values) {
                    return SqlWhereResolver.getWhereListClause(matchType, config, values)
                } else {
                    // cannot have empty lists for the SQL IN clause
                    return null
                }
            } else {
                // gets resolved as a string value
                def value = AttributeValueResolver.getAttributeValue(config, matchInput)
                return SqlWhereResolver.getWhereClause(matchType, config, value)
            }
        }
        log.trace("Found ${whereAndValues.size()} statements for ${matchConfidence.ruleName}. Now checking if all has a value")
        if (whereAndValues.every { it?.value != null }) {
            def returnValue = new WhereAndValues(ruleName: matchConfidence.ruleName, sql: whereAndValues.sql.join(' AND '), values: whereAndValues.value)
            log.trace("Returning search sql: $returnValue.sql with values: ${returnValue.redactedValues}")
            return returnValue
        } else {
            return null
        }
    }
}

