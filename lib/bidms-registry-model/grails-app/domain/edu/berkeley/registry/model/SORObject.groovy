package edu.berkeley.registry.model

class SORObject implements Serializable {

    String sorPrimaryKey
    Date queryTime
    Person person

    static SORObject getBySorAndObjectKey(String systemOfRecord, String sorPrimaryKey) {
        def sor = SOR.findByName(systemOfRecord)
        return SORObject.findBySorAndSorPrimaryKey(sor, sorPrimaryKey)
    }

    static belongsTo = [sor: SOR]

    static constraints = {
        sorPrimaryKey nullable: false, unique: 'sor'
        queryTime nullable: false
        person nullable: true
    }

    static mapping = {
        table name: 'SORObject'
        id composite: ['sor', 'sorPrimaryKey']
        version false
        sor column: 'sorId', sqlType: 'SMALLINT'
        sorPrimaryKey column: 'sorObjKey', sqlType: 'VARCHAR(64)'
        queryTime column: 'sorQueryTime'
        person column: 'personUID'

    }
}
