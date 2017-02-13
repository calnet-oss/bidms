package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.transform.ConverterConfig

@ConverterConfig(excludes = ["parent"])
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version", "parent"])
class AssignableRoleCategory implements Comparable {
    Integer id
    String categoryName
    boolean roleAsgnUniquePerCat

    static belongsTo = [parent: AssignableRoleCategory]

    static constraints = {
        categoryName unique: true, size: 1..255
        parent nullable: true // only nullable if it's the root category
    }

    static mapping = {
        table name: "AssignableRoleCategory"
        version false
        id column: 'id', sqlType: 'INTEGER'
        categoryName column: 'categoryName', sqlType: 'VARCHAR(255)'
        parent column: 'parentCategoryId', sqlType: 'INTEGER'
        roleAsgnUniquePerCat column: 'roleAsgnUniquePerCat', sqlType: 'BOOLEAN'
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}
