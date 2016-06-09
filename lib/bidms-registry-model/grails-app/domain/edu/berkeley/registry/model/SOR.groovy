package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.transform.ConverterConfig

@ConverterConfig
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients"])
class SOR implements Serializable {
    Integer id
    String name

    static constraints = {
        name nullable: false, unique: true
    }

    static mapping = {
        table name: 'SOR'
        id column: 'sorId', type: "integer", sqlType: 'SMALLINT', generator: 'sequence', params: [sequence: 'sor_seq']
        version false
        name column: 'sorName', sqlType: 'VARCHAR(64)'
    }
}
