package edu.berkeley.registry.model

import edu.berkeley.registry.statustrack.TrackStatusType
import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@ConverterConfig(excludes = ["person"])
@LogicalEqualsAndHashCode(excludes = ["person"])
class TrackStatus implements Comparable {
    Long id
    TrackStatusType trackStatusType
    Date timeCreated
    String description

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['trackStatusType']
        trackStatusType nullable: false
        timeCreated nullable: true // assigned automatically by db trigger
        description nullable: true, size: 1..256
    }

    static mapping = {
        table name: "TrackStatus"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'TrackStatus_seq'], sqlType: 'BIGINT'
        person column: TrackStatus.getUidColumnName(), sqlType: 'VARCHAR(64)'
        timeCreated column: 'timeCreated', insertable: false, updateable: false
        trackStatusType column: 'trackStatusType', sqlType: 'VARCHAR(64)'
        description column: 'description', sqlType: 'VARCHAR(256)'


    }
    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("TrackStatus", "uid")
    }

    int compareTo(obj) {
        return logicalHashCode() <=> obj.logicalHashCode() ?: hashCode() <=> obj.hashCode()
    }

}
