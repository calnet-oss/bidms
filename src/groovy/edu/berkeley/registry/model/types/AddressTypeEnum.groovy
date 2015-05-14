package edu.berkeley.registry.model.types

/**
 * The different types of addresses from SOR JSON that translate into a
 * AddressType.
 */
enum AddressTypeEnum {
    directoryPrimaryAddress,
    directorySecondaryAddress

    String getName() {
        return name()
    }
}
