package edu.berkeley.registry.model.auth

class RegistryRole implements Serializable {
    private static final long serialVersionUID = 1

    Integer id
    String authority

    RegistryRole(String authority) {
        this()
        this.authority = authority
    }

    @Override
    int hashCode() {
        authority?.hashCode() ?: 0
    }

    @Override
    boolean equals(other) {
        is(other) || (other instanceof RegistryRole && other.authority == authority)
    }

    @Override
    String toString() {
        authority
    }

    static constraints = {
        authority blank: false, unique: true, size: 1..127
    }

    static mapping = {
        table name: "RegistryRole"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'RegistryRole_seq'], sqlType: 'INTEGER'
        authority column: "authority", sqlType: 'VARCHAR(127)'

    }
}
