package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.transform.ConverterConfig

@ConverterConfig
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients"])
class TelephoneType implements Comparable {
    Integer id
    String telephoneTypeName

    static constraints = {
        telephoneTypeName unique: true, size: 1..64
    }

    static mapping = {
        table name: "TelephoneType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        telephoneTypeName column: 'telephoneTypeName', sqlType: 'VARCHAR(64)'
    }

    int compareTo(obj) {
        return hashCode() <=> obj.hashCode()
    }
}
