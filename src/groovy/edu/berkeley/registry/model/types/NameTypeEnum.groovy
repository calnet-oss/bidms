package edu.berkeley.registry.model.types

import edu.berkeley.registry.model.NameType

/**
 * The different types of names from SOR JSON that translate into a
 * NameType.
 */
enum NameTypeEnum implements TypeEnum<NameType>, PrioritizedEnum {
    sorPrimaryName,
    sorPreferredName,
    directoryDisplayName,
    ldapBerkeleyEduName

    /**
     * The first listed is highest priority.
     */
    static NameTypeEnum[] priorityList = [
            directoryDisplayName,
            sorPreferredName,
            sorPrimaryName,
            ldapBerkeleyEduName
    ]

    /**
     * Lower value is higher priority.
     */
    static Map<NameTypeEnum, Integer> priorityMap = [:]
    static {
        for (int i = 0; i < priorityList.length; i++) {
            priorityMap[priorityList[i]] = i
        }
    }

    NameType get() {
        NameType nameType = NameType.findByTypeName(name())
        if (nameType == null)
            throw new RuntimeException("NameType ${name()} could not be found")
        return nameType
    }

    String getName() {
        return name()
    }

    Integer getId() {
        return get().id
    }

    Integer getPriority() {
        Integer pri = priorityMap[this]
        return (pri != null ? pri : Integer.MAX_VALUE)
    }

    static NameTypeEnum getEnum(NameType t) {
        return valueOf(NameTypeEnum, t?.typeName)
    }

    /**
     * Get the NameType priority, for purposes of provisioning, by passing
     * in a NameType.
     *
     * @param sor The NameType to get the priority for.
     * @return The priority rank, starting with 0.  Priority 0 is highest
     *         priority.  Returns Integer.MAX_VALUE if the NameType isn't
     *         prioritized.
     */
    static int getPriority(NameType nameType) {
        if (!nameType) throw new RuntimeException("nameType cannot be null")
        NameTypeEnum e = getEnum(nameType)
        if (!e) {
            return Integer.MAX_VALUE
        } else {
            return e.priority
        }
    }
}
