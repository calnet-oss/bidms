package edu.berkeley.calnet.ucbmatch.util

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class AttributeValueResolverSpec extends Specification {

    @Shared
    Map matchInput = [
            systemOfRecord: "SIS",
            identifier    : "SI12345",

            names         : [[
                                     type  : "official",
                                     given : "Pamela",
                                     family: "Anderson"],
                             [
                                     given : "Pam",
                                     family: "Anderson"]
            ],
            dateOfBirth   : "1983-03-18",
            identifiers   : [[
                                     type      : "national",
                                     identifier: "3B902AE12DF55196"],
                             [
                                     type      : "enterprise",
                                     identifier: "ABCD1234"]
            ]

    ]

    @Unroll
    def "test that a config object with no path and an attribute resolves the correct value"() {
        setup:
        def config = new MatchAttributeConfig(name: name, attribute: attribute)

        expect:
        AttributeValueResolver.getAttributeValue(config, matchInput) == expected

        where:
        name    | attribute        | expected
        "dob"   | "dateOfBirth"    | matchInput.dateOfBirth
        "sor"   | "systemOfRecord" | matchInput.systemOfRecord
        "sorid" | "identifier"     | matchInput.identifier
    }

    @Unroll
    def "test that a config object with a path, an attribute, and perhaps a group resolves the correct value"() {
        setup:
        def config = new MatchAttributeConfig(name: 'alias', path: path, attribute: attribute, group: group)

        expect:
        AttributeValueResolver.getAttributeValue(config, matchInput) == expected

        where:
        path          | attribute    | group        || expected
        'names'       | 'given'      | null         || 'Pam'
        'names'       | 'given'      | 'official'   || 'Pamela'
        'names'       | 'family'     | null         || 'Anderson'
        'names'       | 'family'     | 'official'   || 'Anderson'
        'identifiers' | 'identifier' | 'national'   || '3B902AE12DF55196'
        'identifiers' | 'identifier' | 'enterprise' || 'ABCD1234'
        'identifiers' | 'identifier' | null         || '3B902AE12DF55196'
    }

    @Unroll
    def "test that null like values works with default and custom regular expressions for nullEquivilents"() {
        setup:
        def config = new MatchAttributeConfig(name: 'nullEquiv', attribute: 'nullAttribute')
        if (nullEquivilants != null) {
            config.nullEquivalents = nullEquivilants
        }

        expect:
        AttributeValueResolver.getAttributeValue(config, [nullAttribute: attributeValue]) == expectedOutcome

        where:
        attributeValue | nullEquivilants                | expectedOutcome
        "    "         | null                           | null
        "000-00-0000"  | null                           | null
        "xxxxxx"       | null                           | "xxxxxx"
        "xxxxxx"       | [/[x]+/]                       | null
        "    "         | [/[x]+/]                       | "    "
        "000-00-0000"  | [/[x]+/]                       | "000-00-0000"
        "xxxxxx"       | []                             | "xxxxxx"
        "    "         | [/[x]+/]                       | "    "
        "000-00-0000"  | [/[x]+/]                       | "000-00-0000"
        "____00000"    | [/[-_]+0{4,5}/, /[-_]+9{4,5}/] | null
        "_____0000"    | [/[-_]+0{4,5}/, /[-_]+9{4,5}/] | null
        "______000"    | [/[-_]+0{4,5}/, /[-_]+9{4,5}/] | "______000"
        "____99999"    | [/[-_]+0{4,5}/, /[-_]+9{4,5}/] | null
        "_____9999"    | [/[-_]+0{4,5}/, /[-_]+9{4,5}/] | null
        "______999"    | [/[-_]+0{4,5}/, /[-_]+9{4,5}/] | "______999"
        "_____0909"    | [/[-_]+0{4,5}/, /[-_]+9{4,5}/] | "_____0909"
        "123450000"    | [/[-_]+0{4,5}/, /[-_]+9{4,5}/] | "123450000"
    }
}
