package edu.berkeley.registry.model

import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode(excludes = ["person"])
class Address {
    Long id
    AddressType addressType
    SORObject sorObject
    String address1
    String address2
    String address3
    String city
    String regionState
    String postalCode
    String country

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['sorObject', 'addressType', 'address1', 'address2', 'address3', 'city', 'regionState', 'postalCode', 'country']
        address1 nullable: true, size: 1..255
        address2 nullable: true, size: 1..255
        address3 nullable: true, size: 1..255
        city nullable: true, size: 1..255
        regionState nullable: true, size: 1..255
        postalCode nullable: true, size: 1..64
        country nullable: true, size: 1..255

    }

    static mapping = {
        table name: "Address"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'Address_seq'], sqlType: 'BIGINT'
        addressType column: 'addressTypeId', sqlType: 'SMALLINT'
        person column: Address.getUidColumnName(), sqlType: 'VARCHAR(64)'
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        address1 column: 'address1', sqlType: 'VARCHAR(255)'
        address2 column: 'address2', sqlType: 'VARCHAR(255)'
        address3 column: 'address3', sqlType: 'VARCHAR(255)'
        city column: 'city', sqlType: 'VARCHAR(255)'
        regionState column: 'regionState', sqlType: 'VARCHAR(255)'
        postalCode column: 'postalCode', sqlType: 'VARCHAR(64)'
        country column: 'country', sqlType: 'VARCHAR(255)'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("Address", "uid")
    }
}
