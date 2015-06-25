package edu.berkeley.registry.model.types

import edu.berkeley.registry.model.TelephoneType

/**
 * The different types of telephones from SOR JSON that translate into a
 * TelephoneType.
 */
enum TelephoneTypeEnum implements TypeEnum<TelephoneType> {
    directoryOfficePhone,
    directoryFaxPhone

    TelephoneType telephoneType

    TelephoneType get() {
        if (!telephoneType)
            telephoneType = TelephoneType.findByTelephoneTypeName(name())
        if (telephoneType == null)
            throw new RuntimeException("TelephoneType ${name()} could not be found")
        return telephoneType
    }

    String getName() {
        return name()
    }

    Integer getId() {
        return get().id
    }

    static TelephoneTypeEnum getEnum(TelephoneType t) {
        return valueOf(TelephoneTypeEnum, t?.telephoneTypeName)
    }
}