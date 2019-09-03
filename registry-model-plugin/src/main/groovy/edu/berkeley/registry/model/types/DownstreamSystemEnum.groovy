package edu.berkeley.registry.model.types

import edu.berkeley.registry.model.DownstreamSystem

enum DownstreamSystemEnum implements TypeEnum<DownstreamSystem> {
    LDAP, LDAP_NAMESPACE, LDAP_NAMESPACE_SECONDARY, AD, LDAP_AUTHORIZATIONS

    DownstreamSystem get() {
        return DownstreamSystem.findByName(name())
    }

    String getName() {
        return name()
    }

    Integer getId() {
        return get().id
    }

    /**
     * Get the DownstreamSystemEnum by passing in a DownstreamSystem.
     */
    static DownstreamSystemEnum getEnum(DownstreamSystem sys) {
        return valueOf(DownstreamSystemEnum, sys?.name)
    }
}
