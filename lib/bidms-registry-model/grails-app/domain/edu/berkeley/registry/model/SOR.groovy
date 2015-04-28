package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode
class SOR implements Serializable {
    Integer id
    String name

    static constraints = {
        // 'unique' GRAILS BUG: UNCOMMENT WHEN FIXED: https://jira.grails.org/browse/GRAILS-11600
        name nullable: false/*, unique: true*/
    }

    static mapping = {
        table name: 'SOR'
        id column: 'sorId', type: "integer", sqlType: 'SMALLINT', generator: 'sequence', params: [sequence: 'sor_seq']
        version false
        name column: 'sorName', sqlType: 'VARCHAR(64)'
    }
}
