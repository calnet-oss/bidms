package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.transform.ConverterConfig

@ConverterConfig
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version"])
class AppointmentType implements Comparable {
    Integer id
    String apptTypeName

    static constraints = {
        apptTypeName unique: true, size: 1..64
    }

    static mapping = {
        table name: "AppointmentType"
        version false
        id column: 'id', sqlType: 'SMALLINT'
        apptTypeName column: 'apptTypeName', sqlType: 'VARCHAR(64)'
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}
