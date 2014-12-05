package edu.berkeley.calnet.ucbmatch.config

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
        config.matchAttributes = builder.matchAttributes
    }

    void confidences(Closure closure) {
        MatchConfidencesDelegate builder = new MatchConfidencesDelegate(config.matchAttributes)
        closure.delegate = builder
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call()
        config.canonicalConfidences = builder.canonicalConfidences
        config.potentialConfidences = builder.potentialConfidences
    }

    // Builds a list of matchAttributes by dynamically invoking each method in the closure 'attributes'
    private class MatchAttributesDelegate {
        List<MatchAttribute> matchAttributes = []

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
        MatchAttribute matchAttribute

        MatchAttributeDelegate(String name) {
            this.matchAttribute = new MatchAttribute(name: name)
        }

        void setProperty(String name, value) {
            if(matchAttribute.hasProperty(name)) {
                matchAttribute.setProperty(name, value)
            } else {
                throw new MissingPropertyException(name, MatchAttribute)
            }
        }

        /**
         * assigns a SearchSettings as the delegate and executes the closure
         * @param closure
         */
        void search(Closure closure) {
            def search = new MatchAttribute.SearchSettings()
            closure.delegate = search
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.call()
            matchAttribute.search = search
        }
    }

    // Creates and set canonical and potential confidences, validating that the attributeNames are present
    private class MatchConfidencesDelegate {

        private List<String> matchAttributeNames
        List<List<String>> canonicalConfidences = []
        List<Map<String, String>> potentialConfidences = []

        MatchConfidencesDelegate(List<MatchAttribute> matchAttributes) {
            assert matchAttributes
            this.matchAttributeNames = matchAttributes.name
        }

        void canonical(String value) {
            assert value in matchAttributeNames

            canonicalConfidences << [value]
        }

        void canonical(Object[] values) {
            def names = values.flatten() as List<String>
            assert names.every { it in matchAttributeNames}

            canonicalConfidences << names
        }


        void potential(Map<String, String> potential) {
            assert potential.keySet().every {it in matchAttributeNames}
            assert potential.values().every { it in MatchConfig.VALID_MATCH_TYPES }

            potentialConfidences << potential
        }
    }
}
