package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.transform.ConverterConfig

@ConverterConfig
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version"])
class IdentifierType implements Comparable {

    Integer id
    String idName

    static constraints = {
        idName unique: true
    }

    static mapping = {
        table name: "IdentifierType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        idName column: 'idName', sqlType: 'VARCHAR(64)'
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}
