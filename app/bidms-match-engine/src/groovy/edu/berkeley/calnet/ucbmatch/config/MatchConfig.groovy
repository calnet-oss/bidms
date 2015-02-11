package edu.berkeley.calnet.ucbmatch.config

import groovy.transform.ToString

@ToString(includeNames = true)
class MatchConfig {
    static enum MatchType {
        EXACT,
        SUBSTRING,
        DISTANCE

        static List CANONICAL_TYPES = [EXACT, SUBSTRING]
        static List POTENTIAL_TYPES = [EXACT, SUBSTRING, DISTANCE]
    }

    String matchTable
    MatchReference matchReference
    List<MatchAttributeConfig> matchAttributeConfigs = []
    List<Map<String, MatchType>> canonicalConfidences = []
    List<Map<String, MatchType>> potentialConfidences = []
}
