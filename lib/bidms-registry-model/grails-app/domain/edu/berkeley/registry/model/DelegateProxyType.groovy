package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode
class DelegateProxyType {

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
}
