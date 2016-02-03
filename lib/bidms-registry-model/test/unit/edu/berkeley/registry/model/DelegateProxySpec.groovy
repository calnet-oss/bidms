package edu.berkeley.registry.model

import edu.berkeley.util.domain.LogicalEqualsAndHashCodeInterface
import spock.lang.Specification

class DelegateProxySpec extends Specification {
    void "confirm DelegateProxy using LogicalEqualsAndHashCode annotation"() {
        given:
            DelegateProxy obj = new DelegateProxy()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }

    void "confirm DelegateProxy LogicalEqualsAndHashCode excludes"() {
        given:
            DelegateProxy obj = new DelegateProxy()
        expect:
            DelegateProxy.logicalHashCodeExcludes.contains("delegateProxyPerson")
    }
}
