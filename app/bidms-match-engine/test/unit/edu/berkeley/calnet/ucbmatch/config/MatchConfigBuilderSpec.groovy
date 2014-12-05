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
                    attribute = "some:attribute"
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
                'id' {
                    description = "Other description"
                }
            }
        }
        configClosure.resolveStrategy = Closure.DELEGATE_ONLY
        configClosure.delegate = sut

        when:
        configClosure.call()

        then:
        sut.config.matchAttributes.size() == 2
        sut.config.matchAttributes[0].name == 'sor'
        sut.config.matchAttributes[0].description == 'Some description'
        sut.config.matchAttributes[0].column == 'columnName'
        sut.config.matchAttributes[0].attribute == 'some:attribute'
        sut.config.matchAttributes[0].group == 'groupName'
        sut.config.matchAttributes[0].caseSensitive
        sut.config.matchAttributes[0].alphanumeric
        sut.config.matchAttributes[0].invalidates
        sut.config.matchAttributes[0].search.exact
        sut.config.matchAttributes[0].search.substring
        sut.config.matchAttributes[0].search.distance == 3
        sut.config.matchAttributes[1].name == 'id'
        sut.config.matchAttributes[1].description == 'Other description'
        !sut.config.matchAttributes[1].column
        !sut.config.matchAttributes[1].attribute
        !sut.config.matchAttributes[1].group
        !sut.config.matchAttributes[1].caseSensitive
        !sut.config.matchAttributes[1].alphanumeric
        !sut.config.matchAttributes[1].invalidates
        !sut.config.matchAttributes[1].search
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
        sut.config.matchAttributes = ['attr1', 'attr2', 'attr3'].collect {
            new MatchAttribute(name: it)
        }
    }
}
