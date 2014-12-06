package edu.berkeley.calnet.ucbmatch.util

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig

class AttributeValueResolver {
    private AttributeValueResolver() {}

    static String getAttributeValue(MatchAttributeConfig config, String systemOfRecord, String identifier, Map sorAttributes) {
        switch (config.property) {
            case 'systemOfRecord':
                return systemOfRecord
            case 'identifier':
                return identifier
            default: // Not the specific cases, then resolve the value from the sorAttributes
                return getAttributeValueFromSorAttributes(config, sorAttributes)
        }
    }

    private static String getAttributeValueFromSorAttributes(MatchAttributeConfig config, Map sorAttributes) {
        if (config.path) {
            // Find the candidates in the given path
            def candidates = sorAttributes[config.path] as List<Map>
            // If no candidates return null
            if (!candidates) {
                return null
            }
            // If the config has a group specified, find the element with type = "group" otherwise take the first element from candidates that
            // does not have a type, and finally try with the first one

            Map candidate = config.group ? candidates.find { it.type == config.group } : (candidates.find { !it.type } ?: candidates.first())

            return normalizeValue(config, candidate.getAt(config.attribute))
        } else {
            return normalizeValue(config, sorAttributes.getAt(config.attribute))
        }

    }

    private static String normalizeValue(MatchAttributeConfig matchAttributeConfig, def value) {
        // Current implementation does not normalize values (eg. truncate when null like values)
        value
    }

}
