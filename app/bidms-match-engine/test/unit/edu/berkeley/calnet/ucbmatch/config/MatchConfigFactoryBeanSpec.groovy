package edu.berkeley.calnet.ucbmatch.config

import spock.lang.Specification

class MatchConfigFactoryBeanSpec extends Specification {
    def "test that parseConfig will return a valid config"() {
        setup:
        def config = """
            attributes {
                'sor' {
                    description = "sor description"
                    column = "sorColumn"
                }
                'name' {
                    description = "name description"
                    column = "nameColumn"
                }
            }
            confidences {
                canonical 'sor'
                canonical 'sor','name'
                potential sor: 'exact', name:'distance'
            }
        """

        when:
        def matchConfig = MatchConfigFactoryBean.parseConfig(config)

        then:
        matchConfig.matchAttributeConfigs.name == ['sor','name']
        matchConfig.canonicalConfidences == [['sor'],['sor','name']]
        matchConfig.potentialConfidences == [[sor:'exact',name:'distance']]
    }
}
