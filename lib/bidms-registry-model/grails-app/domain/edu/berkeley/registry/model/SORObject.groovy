package edu.berkeley.registry.model

class SORObject implements Serializable {

    String sorObjectKey
    Date queryTime
    long jsonVersion
    String jsonObject

    static belongsTo = [sor: SOR]
    static constraints = {
        sorObjectKey nullable: false, unique: 'sor'
        queryTime nullable: false
        jsonVersion nullable: false
        jsonObject nullable: false
    }
    static mapping = {
        table name: 'SORObject'
        id composite: ['sor', 'sorObjectKey']
        version false
        sor column: 'sorId', sqlType: 'SMALLINT'
        sorObjectKey column: 'sorObjKey', sqlType: 'VARCHAR(64)'
        queryTime column: 'sorQueryTime'
        jsonVersion column: 'jsonVersion', sqlType: 'INTEGER'
        jsonObject column: 'jsonObj', sqlType: 'JSONB'
    }


}
