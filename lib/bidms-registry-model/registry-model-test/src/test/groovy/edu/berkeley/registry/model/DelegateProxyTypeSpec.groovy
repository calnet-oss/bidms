package edu.berkeley.registry.model

class DelegateProxyTypeSpec extends AbstractDomainObjectSpec {

    public Class<?> getDomainClass() { return DelegateProxyType }

    void "confirm DelegateProxyType using LogicalEqualsAndHashCode annotation"() {
        expect:
        testIsLogicalEqualsAndHashCode()
    }

    void "confirm DelegateProxyType LogicalEqualsAndHashCode excludes"() {
        expect:
        testExcludes([])
    }

    void "confirm Identifier logicalHashCodeProperties"() {
        expect:
        testHashCodeProperties(["delegateProxyTypeName"])
    }
}
