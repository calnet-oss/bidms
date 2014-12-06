package edu.berkeley.calnet.ucbmatch.config

import edu.berkeley.calnet.ucbmatch.database.NullIdGenerator
import spock.lang.Specification
import spock.lang.Unroll

class MatchConfigBuilderSpec extends Specification {
    def sut = new MatchConfigBuilder()

    def "test creating referenceId element"() {
        setup:
        def configClosure = {
            referenceId {
                idGenerator = NullIdGenerator
                responseType = 'uid'
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        expect:
        sut.config.matchReference == null

        when:
        configClosure.call()

        then:
        sut.config.matchReference.idGenerator == NullIdGenerator
        sut.config.matchReference.responseType == 'uid'
    }

    def "test creating attributes in builder"() {
        setup:
        def configClosure = {
            attributes {
                'sor' {
                    description = "Some description"
                    column = "columnName"
                    property = "systemOfRecord"
                    path = "somePath"
                    attribute = "someAttribute"
                    group = "groupName"
                    caseSensitive = true
                    alphanumeric = true
                    invalidates = true
                    search {
                        exact = true
                        substring = true
                        distance = 3
                    }
                }
                'sorid' {
                    description = "Other description"
                }
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        sut.config.matchAttributeConfigs.size() == 2
        sut.config.matchAttributeConfigs[0].name == 'sor'
        sut.config.matchAttributeConfigs[0].description == 'Some description'
        sut.config.matchAttributeConfigs[0].column == 'columnName'
        sut.config.matchAttributeConfigs[0].property == 'systemOfRecord'
        sut.config.matchAttributeConfigs[0].path == 'somePath'
        sut.config.matchAttributeConfigs[0].attribute == 'someAttribute'
        sut.config.matchAttributeConfigs[0].group == 'groupName'
        sut.config.matchAttributeConfigs[0].caseSensitive
        sut.config.matchAttributeConfigs[0].alphanumeric
        sut.config.matchAttributeConfigs[0].invalidates
        sut.config.matchAttributeConfigs[0].search.exact
        sut.config.matchAttributeConfigs[0].search.substring
        sut.config.matchAttributeConfigs[0].search.distance == 3
        sut.config.matchAttributeConfigs[1].name == 'sorid'
        sut.config.matchAttributeConfigs[1].description == 'Other description'
        !sut.config.matchAttributeConfigs[1].column
        !sut.config.matchAttributeConfigs[1].attribute
        !sut.config.matchAttributeConfigs[1].group
        !sut.config.matchAttributeConfigs[1].caseSensitive
        !sut.config.matchAttributeConfigs[1].alphanumeric
        !sut.config.matchAttributeConfigs[1].invalidates
        !sut.config.matchAttributeConfigs[1].search
    }

    def "test when attributes is missing config closure"() {
        setup:
        def configClosure = {
            attributes {
                'sor'()
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        thrown(AssertionError)
    }

    def "test when confidences are missing attributes"() {
        setup:
        def configClosure = {
            confidences {}
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        thrown(AssertionError)
    }

    @Unroll
    def "test adding canonical confidences to config"() {
        setup:
        createAttributes()
        def configClosure = {
            confidences {
                canonical args
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        sut.config.canonicalConfidences == [expected]

        where:
        args               | expected
        'attr1'            | ['attr1']
        ['attr1']          | ['attr1']
        ['attr1', 'attr2'] | ['attr1', 'attr2']
    }

    def "test adding invalid canonical confidences to config"() {
        setup:
        createAttributes()
        def configClosure = {
            confidences {
                canonical 'unknown'
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        thrown(AssertionError)
    }
    def "test addding multiple canonical confidences to config"() {
        setup:
        createAttributes()
        def configClosure = {
            confidences {
                canonical 'attr1'
                canonical 'attr2','attr3'
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        sut.config.canonicalConfidences.size() == 2
        sut.config.canonicalConfidences == [['attr1'],['attr2','attr3']]
    }

    def "test adding potential confidences to config"() {
        setup:
        createAttributes()
        def configClosure = {
            confidences {
                potential args
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        sut.config.potentialConfidences == [expected]

        where:
        args                                | expected
        [attr1: 'exact']                    | [attr1: 'exact']
        [attr1: 'exact', attr2: 'distance'] | [attr1: 'exact', attr2: 'distance']
    }


    void createAttributes() {
        sut.config.matchAttributeConfigs = ['attr1', 'attr2', 'attr3'].collect {
            new MatchAttributeConfig(name: it)
        }
    }
}
