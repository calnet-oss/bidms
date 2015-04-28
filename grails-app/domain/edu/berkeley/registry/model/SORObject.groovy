package edu.berkeley.registry.model

import edu.berkeley.hibernate.usertype.JSONBType
import groovy.json.JsonSlurper

class SORObject implements Serializable {

    String sorPrimaryKey
    Date queryTime
    String objJson
    Integer jsonVersion

    static transients = ['json']

    static SORObject getBySorAndObjectKey(String systemOfRecord, String sorObjectKey) {
        def sorObject = SORObject.where { sor.name == systemOfRecord && sorPrimaryKey == sorObjectKey }.get()
        return sorObject
    }

    static belongsTo = [sor: SOR, person: Person]

    static constraints = {
        sorPrimaryKey nullable: false, unique: 'sor'
        queryTime nullable: false
        person nullable: true
    }

    static mapping = {
        table name: 'SORObject'
        id sqlType: "BIGINT", generator: 'sequence', params: [sequence: 'SORObject_seq']
        version false
        objJson column: 'objJson', type: JSONBType, sqlType: 'jsonb'
        jsonVersion column: 'jsonVersion', sqlType: 'SMALLINT'
        sor column: 'sorId', sqlType: 'SMALLINT'
        sorPrimaryKey column: 'sorObjKey', sqlType: 'VARCHAR(64)'
        queryTime column: 'sorQueryTime'
        person column: 'uid'
    }

    Map getJson() {
        new JsonSlurper().parseText(objJson) as Map
    }
}
