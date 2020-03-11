package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.hibernate.usertype.JSONBType
import edu.berkeley.util.domain.transform.ConverterConfig
import groovy.json.JsonSlurper

@ConverterConfig
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version", "person", "json", "objJson", "queryTime", "rematch"])
class SORObject implements Serializable, Comparable {

    //
    // Know that adding new properties here without excluding them in the
    // @LogicalEqualsAndHashCode will cause a different logical hash code to
    // be calculated which could alter the order of SortedSets in Person
    // (and anywhere else in the model where there is a SortedSet with
    // SORObjects somewhere in the inheritance chain.) This isn't a problem,
    // but you may have to alter the ordering of the expected JSON or XML
    // output in some tests, such as registry-service
    // IdentifiersServiceIntegrationSpec.
    //

    String sorPrimaryKey
    Date queryTime
    String objJson
    Integer jsonVersion
    Long hash
    boolean rematch

    static transients = ['json', 'uid', 'hash']

    static SORObject getBySorAndObjectKey(String systemOfRecord, String sorObjectKey) {
        def sorObject = SORObject.where { sor.name == systemOfRecord && sorPrimaryKey == sorObjectKey }.get()
        return sorObject
    }

    static belongsTo = [sor: SOR, person: Person]

    static constraints = {
        sorPrimaryKey nullable: false, unique: 'sor'
        person nullable: true
    }

    static mapping = {
        table name: 'SORObject'
        id sqlType: "BIGINT", generator: 'sequence', params: [sequence: 'SORObject_seq']
        version false
        objJson column: 'objJson', type: JSONBType, sqlType: 'jsonb'
        jsonVersion column: 'jsonVersion', sqlType: 'SMALLINT'
        sor column: 'sorId', sqlType: 'SMALLINT'
        sorPrimaryKey column: 'sorObjKey', sqlType: 'VARCHAR(255)'
        queryTime column: 'sorQueryTime'
        person column: 'uid'
        hash column: 'hash', sqlType: 'BIGINT'
        rematch column: 'rematch', sqlType: 'BOOLEAN'
    }

    Map getJson() {
        new JsonSlurper().parseText(objJson) as Map
    }

    // this is here for the LogicalEqualsAndHashCode, which otherwise
    // excludes person
    String getUid() {
        return person?.uid
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}
