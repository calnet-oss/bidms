package edu.berkeley.registry.model.types

/**
 * The different types of telephones from SOR JSON that translate into a
 * TelephoneType.
 */
enum TelephoneTypeEnum {
    directoryOfficePhone,
    directoryFaxPhone

    String getName() {
        return name()
    }
}
