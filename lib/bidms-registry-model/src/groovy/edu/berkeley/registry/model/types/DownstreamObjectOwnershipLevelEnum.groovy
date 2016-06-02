package edu.berkeley.registry.model.types

enum DownstreamObjectOwnershipLevelEnum {
    NOT_OWNED(0),
    OWNED(1)

    final int value

    DownstreamObjectOwnershipLevelEnum(int ownershipLevelValue) {
        this.value = ownershipLevelValue
    }
}
