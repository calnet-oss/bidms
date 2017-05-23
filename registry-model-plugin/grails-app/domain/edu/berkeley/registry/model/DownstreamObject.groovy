package edu.berkeley.registry.model

import edu.berkeley.hibernate.usertype.JSONBType
import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.IncludesExcludesInterface
import edu.berkeley.util.domain.transform.ConverterConfig
import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import groovy.json.JsonSlurper

@ConverterConfig(excludes = ["person", "objJson"])
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version", "person", "json"])
class DownstreamObject implements Serializable, Comparable {

    // so that our rendered map includes nulls, which is important for the
    // downstream provisioning engine to know which attributes to clear out
    // downstream
    static class IncludeNullsMap<K, V> extends LinkedHashMap<K, V> implements IncludesExcludesInterface {
        @Override
        List<String> getExcludes() {
            return null
        }

        @Override
        List<String> getIncludes() {
            return null
        }

        @Override
        Boolean getIncludeNulls() {
            return true
        }
    }


    String systemPrimaryKey
    String objJson
    Long hash
    boolean markedForDeletion
    Integer ownershipLevel

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
        person column: DownstreamObject.getUidColumnName(), sqlType: 'VARCHAR(64)'
        hash column: 'hash', sqlType: 'BIGINT'
        markedForDeletion column: 'markedForDeletion', sqlType: 'BOOLEAN', defaultValue: false
        ownershipLevel column: 'ownershipLevel', sqlType: 'INTEGER'
    }

    Map getJson() {
        new JsonSlurper().parseText(objJson) as IncludeNullsMap
    }

    // this is here for the LogicalEqualsAndHashCode, which otherwise
    // excludes person
    String getUid() {
        return person?.uid
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("DownstreamObject", "uid")
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}
