package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.transform.ConverterConfig

@ConverterConfig
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version"])
class AddressType implements Comparable {
    Integer id
    String addressTypeName

    static constraints = {
        addressTypeName unique: true, size: 1..64
    }

    static mapping = {
        table name: "AddressType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        addressTypeName column: 'addressTypeName', sqlType: 'VARCHAR(64)'
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}
