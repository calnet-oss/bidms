package edu.berkeley.calnet.ucbmatch.config

import groovy.transform.ToString

@ToString(includeNames = true)
class MatchAttributeConfig {
    String name
    String description
    String column
    String property
    String path
    String attribute
    String group
    boolean caseSensitive
    boolean alphanumeric
    boolean invalidates
    SearchSettings search

    @ToString(includeNames = true)
    static class SearchSettings {
        boolean exact
        boolean substring
        int distance
    }
}
