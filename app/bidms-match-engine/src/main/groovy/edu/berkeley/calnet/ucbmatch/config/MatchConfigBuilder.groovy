package edu.berkeley.calnet.ucbmatch.config

import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType

class MatchConfigBuilder {

    MatchConfig config = new MatchConfig()

    void matchTable(String tableName) {
        config.matchTable = tableName
    }

    void referenceId(Closure closure) {
        def matchReference = new MatchReference()
        closure.delegate = matchReference
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call()
        config.matchReference = matchReference
    }

    void attributes(Closure closure) {
        def builder = new MatchAttributesDelegate()
        closure.delegate = builder
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call()
        config.matchAttributeConfigs = builder.matchAttributes
    }

    void confidences(Closure closure) {
        MatchConfidencesDelegate builder = new MatchConfidencesDelegate(config.matchAttributeConfigs)
        closure.delegate = builder
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call()
        config.canonicalConfidences = builder.canonicalConfidences
        config.potentialConfidences = builder.potentialConfidences
    }

    // Builds a list of matchAttributeConfigs by dynamically invoking each method in the closure 'attributes'
    private class MatchAttributesDelegate {
        List<MatchAttributeConfig> matchAttributes = []

        @SuppressWarnings("GroovyAssignabilityCheck")
        def invokeMethod(String name, args) {
            assert args.size() == 1
            assert args[0] instanceof Closure

            MatchAttributeDelegate builder = new MatchAttributeDelegate(name)

            def closure = args[0] as Closure
            closure.delegate = builder
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()

            matchAttributes << builder.matchAttribute
        }

    }

    // Creates and sets properties on a matchAttribute by invoking setProperty and handles the search and input closures
    private class MatchAttributeDelegate {
        MatchAttributeConfig matchAttribute

        MatchAttributeDelegate(String name) {
            this.matchAttribute = new MatchAttributeConfig(name: name)
        }

        void setProperty(String name, value) {
            if (matchAttribute.hasProperty(name)) {
                matchAttribute.setProperty(name, value)
            } else {
                throw new MissingPropertyException(name, MatchAttributeConfig)
            }
        }

        /**
         * assigns a SearchSettings as the delegate and executes the search closure
         */
        void search(Closure closure) {
            def search = new MatchAttributeConfig.SearchSettings()
            closure.delegate = search
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()
            matchAttribute.search = search
        }

        /**
         * assigns a InputSettings as the delegate and executes the input closure
         */
        void input(Closure closure) {
            def input = new MatchAttributeConfig.InputSettings()
            closure.delegate = input
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()
            matchAttribute.input = input
        }
    }

    // Creates and set canonical and potential confidences, validating that the attributeNames are present
    private class MatchConfidencesDelegate {

        private List<String> matchAttributeNames
        List<MatchConfidence> canonicalConfidences = []
        List<MatchConfidence> potentialConfidences = []

        MatchConfidencesDelegate(List<MatchAttributeConfig> matchAttributes) {
            assert matchAttributes
            this.matchAttributeNames = matchAttributes.name
        }

        void canonical(Map<String, MatchType> canonical, String rule = null) {
            validateKeysAndValues("canonical", canonical, MatchType.CANONICAL_TYPES)
            rule = rule ?: "Canonical #${canonicalConfidences.size()+1}"
            canonicalConfidences << new MatchConfidence(ruleName: rule, confidence: canonical)
        }

        void potential(Map<String, MatchType> potential, String rule = null) {
            validateKeysAndValues("potential", potential, MatchType.POTENTIAL_TYPES)
            rule = rule ?: "Potential #${potentialConfidences.size() + 1}"

            potentialConfidences << new MatchConfidence(ruleName: rule, confidence: potential)
        }

        private void validateKeysAndValues(String type, Map<String, MatchType> set, List validTypes) {
            def wrongKeys = set.findAll { !(it.key in matchAttributeNames) }
            if (wrongKeys) {
                throw new RuntimeException("Keys in: $type ${set.collect { "$it.key: $it.value" }.join(', ')} is mismatching on the following keys: ${wrongKeys*.key}")
            }
            assert set.values().every { it in validTypes }
        }
    }
}