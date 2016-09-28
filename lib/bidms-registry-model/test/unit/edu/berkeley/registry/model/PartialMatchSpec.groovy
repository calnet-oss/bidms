package edu.berkeley.registry.model

import spock.lang.Specification
import spock.lang.Unroll

class PartialMatchSpec extends Specification {

    @Unroll
    def "test serialization of matchRules"() {
        given:
        def sut = new PartialMatch()

        when:
        sut.setMatchRules(rules)

        then:
        sut.matchRuleString == expectedString

        where:
        rules        | expectedString
        null         | ''
        []           | ''
        ['AA']       | 'AA'
        ['AA', 'BB'] | 'AA|BB'
    }

    @Unroll
    def "test deserialization of matchRules"() {
        given:
        def sut = new PartialMatch(matchRuleString: matchRuleString)

        expect:
        sut.getMatchRules() == expectedRules

        where:
        matchRuleString | expectedRules
        null            | []
        ''              | []
        'AA'            | ['AA']
        'AA|BB'         | ['AA', 'BB']
    }
}
