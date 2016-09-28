package edu.berkeley.calnet.ucbmatch

import groovy.transform.Canonical

@Canonical
class SearchResult {
    String ruleName
    Set<Map<String, Object>> rows
}
