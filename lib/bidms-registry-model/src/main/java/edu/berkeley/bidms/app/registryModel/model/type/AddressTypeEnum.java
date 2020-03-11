package edu.berkeley.registry.model.types

import edu.berkeley.registry.model.AddressType

/**
 * The different types of addresses from SOR JSON that translate into a
 * AddressType.
 */
enum AddressTypeEnum implements TypeEnum<AddressType> {
    directoryPrimaryAddress,
    directorySecondaryAddress,
    hrmsHomeAddress

    AddressType get() {
        AddressType addressType = AddressType.findByAddressTypeName(name())
        if (addressType == null)
            throw new RuntimeException("AddressType ${name()} could not be found")
        return addressType
    }

    String getName() {
        return name()
    }

    Integer getId() {
        return get().id
    }

    static AddressTypeEnum getEnum(AddressType t) {
        return valueOf(AddressTypeEnum, t?.addressTypeName)
    }
}
