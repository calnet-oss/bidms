package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.transform.ConverterConfig

@ConverterConfig
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version"])
class NameType implements Comparable {

    Integer id
    String typeName

    static constraints = {
        typeName unique: true
    }

    static mapping = {
        table name: "NameType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        typeName column: 'typeName', sqlType: 'VARCHAR(64)'
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}
