package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.transform.ConverterConfig

@ConverterConfig
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients"])
class EmailType implements Comparable {
    Integer id
    String emailTypeName

    static constraints = {
        emailTypeName unique: true, size: 1..64
    }

    static mapping = {
        table name: "EmailType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        emailTypeName column: 'emailTypeName', sqlType: 'VARCHAR(64)'
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}
