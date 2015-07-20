package edu.berkeley.registry.model

import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@ConverterConfig(excludes = ["person", "sorObject"])
@LogicalEqualsAndHashCode(excludes = ["person"])
class PersonName {

    Long id
    NameType nameType
    SORObject sorObject
    String honorific
    String givenName
    String middleName
    String surName
    String suffix
    String fullName
    boolean isPrimary

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['sorObject', 'nameType']
        honorific nullable: true
        givenName nullable: true
        middleName nullable: true
        surName nullable: true
        suffix nullable: true
        fullName nullable: true
    }

    static mapping = {
        table name: "PersonName"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'PersonName_seq'], sqlType: 'BIGINT'
        person column: PersonName.getUidColumnName(), sqlType: 'VARCHAR(64)'
        nameType column: 'nameTypeId', sqlType: 'SMALLINT'
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        honorific column: 'honorific', sqlType: 'VARCHAR(32)'
        givenName column: 'givenName', sqlType: 'VARCHAR(127)'
        middleName column: 'middleName', sqlType: 'VARCHAR(127)'
        surName column: 'surName', sqlType: 'VARCHAR(127)'
        suffix column: 'suffix', sqlType: 'VARCHAR(32)'
        fullName column: 'fullName', sqlType: 'VARCHAR(255)'
        isPrimary column: 'isPrimary', sqlType: 'BOOLEAN'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("PersonName", "uid")
    }
}
