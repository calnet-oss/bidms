package edu.berkeley.registry.model

class PersonName {

    Long id
    NameType nameType
    String honorific
    String givenName
    String middleName
    String surName
    String suffix
    String fullName

    static belongsTo = [person: Person]

    static constraints = {
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
        person column: 'uid', sqlType: 'VARCHAR(64)'
        nameType column: 'nameTypeId', sqlType: 'SMALLINT'
        honorific column: 'honorific', sqlType: 'VARCHAR(32)'
        givenName column: 'givenName', sqlType: 'VARCHAR(127)'
        middleName column: 'middleName', sqlType: 'VARCHAR(127)'
        surName column: 'surName', sqlType: 'VARCHAR(127)'
        suffix column: 'suffix', sqlType: 'VARCHAR(32)'
        fullName column: 'fullName', sqlType: 'VARCHAR(255)'
    }
}
