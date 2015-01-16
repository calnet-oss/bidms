package edu.berkeley.registry.model

class SOR implements Serializable {
    String name

    static hasMany = [sorObjects: SORObject]

    static constraints = {
        name nullable: false, unique: true
    }

    static mapping = {
        table name: 'SOR'
        id column: 'sorId', sqlType: 'SMALLINT', generator: 'sequence', params: [sequence: 'sorid_seq']
        version false
        name name: 'sorName', sqlType: 'VARCHAR(64)'
    }
}
