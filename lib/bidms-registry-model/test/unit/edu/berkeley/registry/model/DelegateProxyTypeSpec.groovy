package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import spock.lang.Specification

class DelegateProxyTypeSpec extends Specification {

    void "confirm DelegateProxyType using LogicalEqualsAndHashCode annotation"() {
        given:
            DelegateProxyType obj = new DelegateProxyType()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }
}
