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
    List nullEquivalents = [/[0-]+/,/\s+/]
    SearchSettings search

    static MatchAttributeConfig create(Map params) {
        params.inject(new MatchAttributeConfig(search: new SearchSettings())) { MatchAttributeConfig config, entry->
            String name = entry.key
            def value = entry.value
            if(config.hasProperty(name)) {
                config.setProperty(name, value)
            } else if (config.search.hasProperty(name)) {
                config.search.setProperty(name, value)
            } else {
                throw new IllegalArgumentException("Property ${name} not found in config or config.search")
            }
            return config
        }
    }

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
