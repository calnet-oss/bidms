package edu.berkeley.calnet.ucbmatch.config

import groovy.transform.ToString

@ToString(includeNames = true)
class MatchConfig {
    static enum MatchType {
        EXACT,
        SUBSTRING,
        DISTANCE,
        FIXED_VALUE

        static List CANONICAL_TYPES = [EXACT, SUBSTRING, FIXED_VALUE]
        static List POTENTIAL_TYPES = [EXACT, SUBSTRING, FIXED_VALUE, DISTANCE]
    }

    String matchTable
    MatchReference matchReference
    List<MatchAttributeConfig> matchAttributeConfigs = []
    List<Map<String, MatchType>> canonicalConfidences = []
    List<Map<String, MatchType>> potentialConfidences = []
}
