package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.transform.ConverterConfig

@ConverterConfig
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version"])
class DelegateProxyType implements Comparable {

    Integer id
    String delegateProxyTypeName

    static constraints = {
        delegateProxyTypeName unique: true
    }

    static mapping = {
        table name: "DelegateProxyType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        delegateProxyTypeName column: 'delegateProxyTypeName', sqlType: 'VARCHAR(64)'
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}
