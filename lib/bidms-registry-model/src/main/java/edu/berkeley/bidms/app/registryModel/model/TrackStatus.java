package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.hibernate.usertype.JSONBType
import edu.berkeley.registry.statustrack.TrackStatusType
import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

@ConverterConfig(includes = ['id', 'trackStatusType', 'timeCreated', 'description', 'metaData'])
@LogicalEqualsAndHashCode(
        excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version", "person", "metaData", "metaDataJson"],
        changeCallbackClass = TrackStatusHashCodeChangeCallback
)
class TrackStatus implements Comparable {
    static class TrackStatusHashCodeChangeCallback extends PersonCollectionHashCodeChangeHandler<TrackStatus> {
        TrackStatusHashCodeChangeCallback() {
            super("trackStatuses")
        }
    }

    Long id
    TrackStatusType trackStatusType
    Date timeCreated
    String description
    String metaDataJson = '{}'
    Map metaData = [:]

    static transients = ['metaData']

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['trackStatusType']
        trackStatusType nullable: false
        timeCreated nullable: true // assigned automatically by db trigger
        description nullable: true, size: 1..256
        metaDataJson nullable: true
        metaData nullable: true, bindable: true
    }

    static mapping = {
        table name: "TrackStatus"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'TrackStatus_seq'], sqlType: 'BIGINT'
        person column: TrackStatus.getUidColumnName(), sqlType: 'VARCHAR(64)'
        timeCreated column: 'timeCreated', insertable: false, updateable: false
        trackStatusType column: 'trackStatusType', sqlType: 'VARCHAR(64)'
        description column: 'description', sqlType: 'VARCHAR(256)'
        metaDataJson column: 'metaDataJson', type: JSONBType, sqlType: 'jsonb'
    }
    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("TrackStatus", "uid")
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }

    def afterLoad() {
        metaData = new JsonSlurper().parseText(metaDataJson ?: '{}') as Map
    }

    def beforeValidate() {
        metaDataJson = JsonOutput.toJson(metaData ?: [:])
    }
}
