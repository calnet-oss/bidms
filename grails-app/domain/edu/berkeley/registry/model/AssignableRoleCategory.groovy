package edu.berkeley.registry.model

import edu.berkeley.util.domain.transform.ConverterConfig
import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode

@ConverterConfig(excludes = ["parent"])
@LogicalEqualsAndHashCode(excludes = ["parent"])
class AssignableRoleCategory {
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
}
