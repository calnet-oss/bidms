package edu.berkeley.calnet.ucbmatch.util

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class AttributeValueResolverSpec extends Specification {

    @Shared
    Map matchInput = [
            systemOfRecord          : "SIS",
            identifier              : "SI12345",

            names                   : [[
                                               type  : "official",
                                               given : "Pamela",
                                               family: "Anderson"],
                                       [
                                               given : "Pam",
                                               family: "Anderson"]
            ],
            dateOfBirth             : "1983-03-18",
            identifiers             : [[
                                               type      : "national",
                                               identifier: "3B902AE12DF55196"],
                                       [
                                               type      : "enterprise",
                                               identifier: "ABCD1234"]
            ],
            nullValueAttribute1     : "    ",
            nullValueAttribute2     : "000-00-0000",
            customNullValueAttribute: "xxxxxx"

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
        def config = new MatchAttributeConfig(name: 'nullEquiv', attribute: attribute)
        if (nullEquivilants != null) {
            config.nullEquivalents = nullEquivilants
        }

        expect:
        AttributeValueResolver.getAttributeValue(config, matchInput) == expectedOutcome

        where:
        attribute                  | nullEquivilants | expectedOutcome
        "nullValueAttribute1"      | null            | null
        "nullValueAttribute2"      | null            | null
        "customNullValueAttribute" | null            | "xxxxxx"
        "customNullValueAttribute" | [/[x]+/]        | null
        "nullValueAttribute1"      | [/[x]+/]        | "    "
        "nullValueAttribute2"      | [/[x]+/]        | "000-00-0000"
        "customNullValueAttribute" | []              | "xxxxxx"
        "nullValueAttribute1"      | [/[x]+/]        | "    "
        "nullValueAttribute2"      | [/[x]+/]        | "000-00-0000"
    }


}
