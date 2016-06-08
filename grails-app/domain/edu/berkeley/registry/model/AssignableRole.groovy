package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode

@LogicalEqualsAndHashCode
class AssignableRole {
    Integer id
    String roleName

    static belongsTo = [roleCategory: AssignableRoleCategory]

    static constraints = {
        roleName unique: true, size: 1..255
    }

    static mapping = {
        table name: "AssignableRole"
        version false
        id column: 'id', sqlType: 'INTEGER'
        roleName column: 'roleName', sqlType: 'VARCHAR(255)'
        roleCategory column: 'roleCategoryId', sqlType: 'INTEGER'
    }
}
