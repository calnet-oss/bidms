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
    boolean invalidates
    SearchSettings search

    @ToString(includeNames = true)
    static class SearchSettings {
        boolean exact
        Map substring
        boolean caseSensitive
        boolean alphanumeric
        int distance
        void setSubstring() {

            if(!substring.start) {
                throw new IllegalArgumentException("Missing 'start' argument in Map")
            }
            if(!substring.length) {
                throw new IllegalArgumentException("Missing 'length' argument in Map")
            }
        }
    }
}
