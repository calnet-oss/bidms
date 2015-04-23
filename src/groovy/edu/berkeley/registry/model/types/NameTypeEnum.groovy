package edu.berkeley.registry.model.types

/**
 * The different types of names from SOR JSON that translate into a
 * NameType.
 */
enum NameTypeEnum {
    sorPrimaryName,
    sorPreferredName,
    directoryDisplayName,
    ldapBerkeleyEduName

    String getName() {
        return name()
    }
}
