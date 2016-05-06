package edu.berkeley.registry.model

import edu.berkeley.hibernate.usertype.JSONBType
import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode
import groovy.json.JsonSlurper

@LogicalEqualsAndHashCode(excludes = ["person", "json", "objJson"])
class DownstreamObject implements Serializable {

    String systemPrimaryKey
    String objJson
    Long hash
    boolean markedForDeletion

    // hash is a transient because it's always updated by DB trigger
    static transients = ['json', 'uid', 'hash']

    static belongsTo = [system: DownstreamSystem, person: Person]

    static constraints = {
        systemPrimaryKey unique: 'system'
    }

    static mapping = {
        table name: 'DownstreamObject'
        id sqlType: "BIGINT", generator: 'sequence', params: [sequence: 'DownstreamObject_seq']
        version false
        objJson column: 'objJson', type: JSONBType, sqlType: 'jsonb'
        system column: 'systemId', sqlType: 'SMALLINT'
        systemPrimaryKey column: 'sysObjKey', sqlType: 'VARCHAR(255)'
        person column: 'uid'
        hash column: 'hash', sqlType: 'BIGINT'
        markedForDeletion column: 'markedForDeletion', sqlType: 'BOOLEAN', defaultValue: false
    }

    Map getJson() {
        new JsonSlurper().parseText(objJson) as Map
    }

    // this is here for the LogicalEqualsAndHashCode, which otherwise
    // excludes person
    String getUid() {
        return person?.uid
    }
}
