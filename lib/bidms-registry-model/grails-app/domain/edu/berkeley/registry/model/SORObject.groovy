package edu.berkeley.registry.model

class SORObject implements Serializable {

    String sorPrimaryKey
    Date queryTime
    Person person
    String objJson

    static SORObject getBySorAndObjectKey(String systemOfRecord, String sorObjectKey) {
        def sorObject = SORObject.where { sor.name == systemOfRecord && sorPrimaryKey == sorObjectKey }.get()
        return sorObject
    }

    static belongsTo = [sor: SOR]

    static constraints = {
        sorPrimaryKey nullable: false, unique: 'sor'
        queryTime nullable: false
        person nullable: true
    }

    static mapping = {
        table name: 'SORObject'
        id sqlType: "BIGINT", generator: 'sequence', params: [sequence: 'sorobject_seq']
        version false
        objJson column: 'objJson', sqlType: 'TEXT'
        sor column: 'sorId', sqlType: 'SMALLINT'
        sorPrimaryKey column: 'sorObjKey', sqlType: 'VARCHAR(64)'
        queryTime column: 'sorQueryTime'
        person column: 'uid'

    }
}
