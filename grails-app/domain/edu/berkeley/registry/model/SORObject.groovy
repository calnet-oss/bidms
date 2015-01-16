package edu.berkeley.registry.model

class SORObject implements Serializable {

    String sorObjectKey
    Date queryTime
    Person person

    static SORObject getBySorAndObjectKey(String systemOfRecord, String sorObjectKey) {
        def sor = SOR.findByName(systemOfRecord)
        return SORObject.findBySorAndSorObjectKey(sor, sorObjectKey)
    }

    static belongsTo = [sor: SOR]

    static constraints = {
        sorObjectKey nullable: false, unique: 'sor'
        queryTime nullable: false
        person nullable: true
    }

    static mapping = {
        table name: 'SORObject'
        id composite: ['sor', 'sorObjectKey']
        version false
        sor column: 'sorId', sqlType: 'SMALLINT'
        sorObjectKey column: 'sorObjKey', sqlType: 'VARCHAR(64)'
        queryTime column: 'sorQueryTime'
        person column: 'personUID'

    }
}
