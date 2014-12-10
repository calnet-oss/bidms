package edu.berkeley.calnet.ucbmatch.config

import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.*
import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType.*

class MatchConfigBuilder {

    MatchConfig config = new MatchConfig()

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

    // Creates and sets properties on a matchAttribute by invoking setProperty and handle the search closure
    private class MatchAttributeDelegate {
        MatchAttributeConfig matchAttribute

        MatchAttributeDelegate(String name) {
            this.matchAttribute = new MatchAttributeConfig(name: name)
        }

        void setProperty(String name, value) {
            if(matchAttribute.hasProperty(name)) {
                matchAttribute.setProperty(name, value)
            } else {
                throw new MissingPropertyException(name, MatchAttributeConfig)
            }
        }

        /**
         * assigns a SearchSettings as the delegate and executes the closure
         * @param closure
         */
        void search(Closure closure) {
            def search = new MatchAttributeConfig.SearchSettings()
            closure.delegate = search
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()
            matchAttribute.search = search
        }
    }

    // Creates and set canonical and potential confidences, validating that the attributeNames are present
    private class MatchConfidencesDelegate {

        private List<String> matchAttributeNames
        List<Map<String, MatchType>> canonicalConfidences = []
        List<Map<String, MatchType>> potentialConfidences = []

        MatchConfidencesDelegate(List<MatchAttributeConfig> matchAttributes) {
            assert matchAttributes
            this.matchAttributeNames = matchAttributes.name
        }

        void canonical(Map<String,MatchType> canonical) {
            assert canonical.keySet().every {it in matchAttributeNames}
            assert canonical.values().every { it in CANONICAL_TYPES}

            canonicalConfidences << canonical
        }


        void potential(Map<String, MatchType> potential) {
            assert potential.keySet().every {it in matchAttributeNames}
            assert potential.values().every { it in POTENTIAL_TYPES}

            potentialConfidences << potential
        }
    }
}
