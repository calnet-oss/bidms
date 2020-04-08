package edu.berkeley.calnet.ucbmatch.config

import spock.lang.Specification
import spock.lang.Unroll

import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType.DISTANCE
import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType.EXACT
import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType.FIXED_VALUE
import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType.SUBSTRING

class MatchConfigBuilderSpec extends Specification {
    def sut = new MatchConfigBuilder()

    def "test specifying the tableName"() {
        setup:
        def configClosure = {
            matchTable('myTableName')
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        expect:
        sut.config.matchTable == null

        when:
        configClosure.call()

        then:
        sut.config.matchTable == 'myTableName'
    }

    def "test creating referenceId element"() {
        setup:
        def configClosure = {
            referenceId {
                column = 'UID'
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
        sut.config.matchReference.responseType == 'uid'
        sut.config.matchReference.column == 'UID'
    }

    def "test creating attributes in builder"() {
        setup:
        def configClosure = {
            attributes {
                'sor' {
                    description = "Some description"
                    column = "columnName"
                    path = "somePath"
                    outputPath = "someOtherPath"
                    attribute = "someAttribute"
                    group = "groupName"
                    invalidates = true
                    search {
                        caseSensitive = true
                        alphanumeric = true
                        substring = [from: 1, length: 3]
                        distance = 3
                        fixedValue = 'fixMe'
                    }
                }
                'sorid' {
                    description = "Other description"
                }
                'date' {
                    description = "A date attribute"
                    column = 'dateColumn'
                    search {
                        dateFormat = 'yyyy-MM-dd'
                    }
                }
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        sut.config.matchAttributeConfigs.size() == 3
        sut.config.matchAttributeConfigs[0].name == 'sor'
        sut.config.matchAttributeConfigs[0].description == 'Some description'
        sut.config.matchAttributeConfigs[0].column == 'columnName'
        sut.config.matchAttributeConfigs[0].path == 'somePath'
        sut.config.matchAttributeConfigs[0].outputPath == 'someOtherPath'
        sut.config.matchAttributeConfigs[0].attribute == 'someAttribute'
        sut.config.matchAttributeConfigs[0].group == 'groupName'
        sut.config.matchAttributeConfigs[0].invalidates
        sut.config.matchAttributeConfigs[0].search.caseSensitive
        sut.config.matchAttributeConfigs[0].search.alphanumeric
        sut.config.matchAttributeConfigs[0].search.substring == [from: 1, length: 3]
        sut.config.matchAttributeConfigs[0].search.distance == 3
        sut.config.matchAttributeConfigs[0].search.fixedValue == 'fixMe'
        sut.config.matchAttributeConfigs[1].name == 'sorid'
        sut.config.matchAttributeConfigs[1].description == 'Other description'
        !sut.config.matchAttributeConfigs[1].column
        !sut.config.matchAttributeConfigs[1].attribute
        !sut.config.matchAttributeConfigs[1].group
        !sut.config.matchAttributeConfigs[1].invalidates
        !sut.config.matchAttributeConfigs[1].search
        sut.config.matchAttributeConfigs[2].search.dateFormat == 'yyyy-MM-dd'
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
    def "test adding superCanonical confidences to config"() {
        setup:
        createAttributes()
        def configClosure = {
            confidences {
                superCanonical args
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        sut.config.superCanonicalConfidences*.confidence == [expected]
        sut.config.superCanonicalConfidences*.ruleName == ["SuperCanonical #1"]

        where:
        args                               | expected
        [attr1: EXACT]                     | [attr1: EXACT]
        [attr1: EXACT, attr2: FIXED_VALUE] | [attr1: EXACT, attr2: FIXED_VALUE]
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
        sut.config.canonicalConfidences*.confidence == [expected]
        sut.config.canonicalConfidences*.ruleName == ["Canonical #1"]

        where:
        args                             | expected
        [attr1: EXACT]                   | [attr1: EXACT]
        [attr1: EXACT, attr2: SUBSTRING] | [attr1: EXACT, attr2: SUBSTRING]
    }

    @Unroll
    def "test adding invalid superCanonical confidences to config"() {
        setup:
        createAttributes()
        def configClosure = {
            confidences {
                superCanonical attr1: attr
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        thrown(AssertionError)

        where:
        attr      | _
        DISTANCE  | _
        SUBSTRING | _
    }

    def "test adding invalid canonical confidences to config"() {
        setup:
        createAttributes()
        def configClosure = {
            confidences {
                canonical attr1: DISTANCE
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        thrown(AssertionError)
    }

    def "test addding multiple superCanonical confidences to config"() {
        setup:
        createAttributes()
        def configClosure = {
            confidences {
                superCanonical attr1: EXACT
                superCanonical attr2: EXACT, attr3: FIXED_VALUE
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        sut.config.superCanonicalConfidences.size() == 2
        sut.config.superCanonicalConfidences*.confidence == [[attr1: EXACT], [attr2: EXACT, attr3: FIXED_VALUE]]
        sut.config.superCanonicalConfidences*.ruleName == ["SuperCanonical #1", "SuperCanonical #2"]
    }

    def "test addding multiple canonical confidences to config"() {
        setup:
        createAttributes()
        def configClosure = {
            confidences {
                canonical attr1: EXACT
                canonical attr2: EXACT, attr3: SUBSTRING
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        sut.config.canonicalConfidences.size() == 2
        sut.config.canonicalConfidences*.confidence == [[attr1: EXACT], [attr2: EXACT, attr3: SUBSTRING]]
        sut.config.canonicalConfidences*.ruleName == ["Canonical #1", "Canonical #2"]
    }

    @Unroll
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
        sut.config.potentialConfidences*.confidence == [expected]
        sut.config.potentialConfidences*.ruleName == ["Potential #1"]

        where:
        args                                | expected
        [attr1: EXACT]                      | [attr1: EXACT]
        [attr1: EXACT, attr2: DISTANCE]     | [attr1: EXACT, attr2: DISTANCE]
        [attr2: SUBSTRING, attr3: DISTANCE] | [attr2: SUBSTRING, attr3: DISTANCE]
    }


    void createAttributes() {
        sut.config.matchAttributeConfigs = ['attr1', 'attr2', 'attr3'].collect {
            new MatchAttributeConfig(name: it)
        }
    }
}
