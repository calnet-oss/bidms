package edu.berkeley.calnet.ucbmatch.config

import groovy.transform.ToString

@ToString(includeNames = true)
class MatchConfig {
    public static final VALID_MATCH_TYPES = [MATCH_TYPE_DISTANCE, MATCH_TYPE_EXACT]
    public static final String MATCH_TYPE_DISTANCE = 'distance'
    public static final String MATCH_TYPE_EXACT = 'exact'

    List<MatchAttribute> matchAttributes = []
    List<List<String>> canonicalConfidences = []
    List<Map<String, String>> potentialConfidences = []
}
