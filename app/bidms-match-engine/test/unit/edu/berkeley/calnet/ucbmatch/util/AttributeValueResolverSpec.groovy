package edu.berkeley.calnet.ucbmatch.util

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class AttributeValueResolverSpec extends Specification {
    @Shared
            systemOfRecord = "SIS"
    @Shared
            identifier = "SI12345"
    @Shared
            sorAttributes = [
                    "names"      : [
                            ["type"  : "official",
                             "given" : "Pamela",
                             "family": "Anderson"],
                            ["given" : "Pam",
                             "family": "Anderson"]

                    ],
                    "dateOfBirth": "1983-03-18",
                    "identifiers": [
                            ["type"      : "national",
                             "identifier": "3B902AE12DF55196"],
                            ["type"      : "enterprise",
                             "identifier": "ABCD1234"]
                    ]
            ]

    @Unroll
    def "test that a config object with property resolves the correct value"() {
        setup:
        def config = new MatchAttributeConfig(name: "sor", property: property)

        expect:
        AttributeValueResolver.getAttributeValue(config, systemOfRecord, identifier, sorAttributes) == expected

        where:
        property         | expected
        "systemOfRecord" | systemOfRecord
        "identifier"     | identifier
    }

    def "test that a config object with no path and an attribute resolves the correct value"() {
        setup:
        def config = new MatchAttributeConfig(name: 'dob', attribute: 'dateOfBirth')

        expect:
        AttributeValueResolver.getAttributeValue(config, systemOfRecord, identifier, sorAttributes) == '1983-03-18'
    }

    @Unroll
    def "test that a config object with a path, an attribute, and perhaps a group resolves the correct value"() {
        setup:
        def config = new MatchAttributeConfig(name: 'alias', path: path, attribute: attribute, group: group)

        expect:
        AttributeValueResolver.getAttributeValue(config, systemOfRecord, identifier, sorAttributes) == expected

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


}
