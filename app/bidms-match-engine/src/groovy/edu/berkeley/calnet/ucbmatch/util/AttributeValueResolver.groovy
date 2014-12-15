package edu.berkeley.calnet.ucbmatch.util

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig

class AttributeValueResolver {
    private AttributeValueResolver() {}

    static String getAttributeValue(MatchAttributeConfig config, Map matchInput) {
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

    private static String normalizeValue(MatchAttributeConfig matchAttributeConfig, String value) {
        // If the nullEquivalents is not set, return the value
        if(!matchAttributeConfig.nullEquivalents) {
            value
        }
        // Expect matchAttributeConfig ot be a list of Regular Expressions. If any of these matches, it's a null like value
        def nullMatches = matchAttributeConfig.nullEquivalents.any {
            value ==~ it
        }
        return nullMatches ? null : value
    }

}
