package edu.berkeley.registry.model

import grails.converters.JSON
import groovy.json.JsonBuilder

class SORObject implements Serializable {

    String sorPrimaryKey
    Date queryTime
    Person person
    String objJson
    Integer jsonVersion

    static transients = ['json']

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
        jsonVersion column: 'jsonVerion', sqlType: 'SMALLINT'
        sor column: 'sorId', sqlType: 'SMALLINT'
        sorPrimaryKey column: 'sorObjKey', sqlType: 'VARCHAR(64)'
        queryTime column: 'sorQueryTime'
        person column: 'uid'

    }

    /**
     * creates a map representation of the data in the current object
     *
     * @return Map representation of the data
     */
    Map getJson() {
        log.info("creating map for sorObject data: $id")
        Map data = [
            id: id,
            sorPrimaryKey: sorPrimaryKey,
            queryTime: queryTime?.format("yyyy-MM-dd'T'HH:mm:ssZ"),
            objJson: JSON.parse(objJson),
            jsonVersion: jsonVersion,
            sorName: sor.name,
        ]
        if (person) {
            data.person = [
                    uid: person.uid,
                    dateOfBirthMMDD: person.uid,
                    dateOfBirth: person.dateOfBirth?.format("yyyy-MM-dd'T'HH:mm:ssZ"),
                    timeCreated: person.timeCreated?.format("yyyy-MM-dd'T'HH:mm:ssZ"),
                    timeUpdated: person.timeUpdated?.format("yyyy-MM-dd'T'HH:mm:ssZ"),
                    names: person.names.collect { it.fullName }
            ]
        }

        if(log.debugEnabled) log.debug(new JsonBuilder(data).toPrettyString())

        data
    }
}
