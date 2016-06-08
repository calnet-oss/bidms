package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode
class AddressType {
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
}
