package edu.berkeley.registry.model

import org.apache.commons.lang.builder.HashCodeBuilder

// Must implement Serializable because of the composite primary key.
// http://grails.github.io/grails-doc/2.4.4/guide/single.html#ormdsl
class SORObjectChecksum implements Serializable {

    SOR sor
    String sorObjKey
    Long hash
    Integer hashVersion
    Date timeMarker
    long numericMarker

    static constraints = {
        hash nullable: false
        hashVersion nullable: false
        timeMarker nullable: false
        numericMarker nullable: false
    }

    static mapping = {
        table name: 'SORObjectChecksum'
        id composite: ['sor', 'sorObjKey'], generator: 'assigned'
        version false
        hash column: 'hash', sqlType: 'BIGINT'
        hashVersion column: 'hashVersion', sqlType: 'INTEGER'
        timeMarker column: 'timeMarker'
        sor column: 'sorId', sqlType: 'SMALLINT'
        sorObjKey column: 'sorObjKey', sqlType: 'VARCHAR(255)'
        numericMarker column: 'numericMarker', sqlType: 'BIGINT'
    }

    /**
     * http://grails.github.io/grails-doc/2.4.4/guide/single.html#ormdsl
     *
     * Classes with composite primary keys must implement equals() and
     * hashCode().
     */
    boolean equals(other) {
        return (other instanceof SORObjectChecksum ? other.sor?.id == sor.id && other.sorObjKey == sorObjKey : false)
    }

    /**
     * http://grails.github.io/grails-doc/2.4.4/guide/single.html#ormdsl
     *
     * Classes with composite primary keys must implement equals() and
     * hashCode().
     */
    int hashCode() {
        def builder = new HashCodeBuilder()
        builder.append sorId
        builder.append sorObjKey
        return builder.toHashCode()
    }
}
