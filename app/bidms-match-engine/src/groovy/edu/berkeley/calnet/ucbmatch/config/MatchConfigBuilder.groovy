package edu.berkeley.calnet.ucbmatch.config

class MatchConfigBuilder {

    MatchConfig config = new MatchConfig()

    private MatchAttribute matchAttribute

    void attributes(Closure closure) {
        closure.delegate = this
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call()
    }

    void confidences(Closure closure) {
        assert config.matchAttributes
        closure.delegate = this
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call()
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    def methodMissing(String name, args) {
        assert args.size() == 1
        assert args[0] instanceof Closure

        matchAttribute = new MatchAttribute(name: name)

        def closure = args[0] as Closure
        closure.delegate = this
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call()

        config.matchAttributes << matchAttribute
        matchAttribute = null
    }

    void setDescription(String value) {
        matchAttribute.description = value
    }

    void setColumn(String value) {
        matchAttribute.column = value
    }

    void setAttribute(String value) {
        matchAttribute.attribute = value
    }

    void setGroup(String value) {
        matchAttribute.group = value
    }

    void setCaseSensitive(boolean value) {
        matchAttribute.caseSensitive = value
    }

    void setAlphanumeric(boolean value) {
        matchAttribute.alphanumeric = value
    }

    void setInvalidates(boolean value) {
        matchAttribute.invalidates = value
    }

    void search(Closure closure) {
        closure.delegate = this
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        matchAttribute.search = new MatchAttribute.SearchSettings()
        closure.call()
    }

    void setExact(boolean value) {
        matchAttribute.search.exact = value
    }

    void setDistance(int value) {
        matchAttribute.search.distance = value
    }

    void canonical(String value) {
        assert value in matchAttributeNames
        config.canonicalConfidences << [value]
    }

    void canonical(Object[] values) {
        def names = values.flatten() as List<String>
        assert names.every { it in matchAttributeNames}
        config.canonicalConfidences << names
    }


    void potential(Map<String, String> potential) {
        assert potential.keySet().every {it in matchAttributeNames}
        assert potential.values().every { it in MatchConfig.VALID_MATCH_TYPES }

        config.potentialConfidences << potential
    }

    private ArrayList<String> getMatchAttributeNames() {
        config.matchAttributes.name
    }

}
