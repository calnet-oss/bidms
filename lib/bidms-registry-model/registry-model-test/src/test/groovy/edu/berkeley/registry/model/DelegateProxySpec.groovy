package edu.berkeley.registry.model

class DelegateProxySpec extends AbstractDomainObjectSpec {


    public Class<?> getDomainClass() { return DelegateProxy }

    void "confirm DelegateProxy using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm DelegateProxy LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes(["person", "proxyForIdentifier", "proxyForPerson", "proxyForPersonUid"])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["delegateProxyType", "sourceProxyId", "delegateProxySorObject", "delegateProxySecurityKey", "proxyForId"])
    }
}
