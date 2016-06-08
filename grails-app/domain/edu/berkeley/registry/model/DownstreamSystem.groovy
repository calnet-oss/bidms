package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode
class DownstreamSystem implements Serializable {
    Integer id
    String name

    static constraints = {
        name nullable: false, unique: true
    }

    static mapping = {
        table name: 'DownstreamSystem'
        id column: 'systemId', type: "integer", sqlType: 'SMALLINT', generator: 'sequence', params: [sequence: 'DownstreamSystem_seq']
        version false
        name column: 'systemName', sqlType: 'VARCHAR(64)'
    }
}
