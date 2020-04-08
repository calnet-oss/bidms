package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import org.hibernate.FetchMode

@ConverterConfig(excludes = ["person", "sorObject"])
@LogicalEqualsAndHashCode(
        excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version", "person"],
        changeCallbackClass = TelephoneHashCodeChangeCallback
)
class Telephone implements Comparable {

    static class TelephoneHashCodeChangeCallback extends PersonCollectionHashCodeChangeHandler<Telephone> {
        TelephoneHashCodeChangeCallback() {
            super("telephones")
        }
    }

    Long id
    TelephoneType telephoneType
    SORObject sorObject
    String phoneNumber
    String extension

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['sorObject', 'telephoneType', 'phoneNumber']
        extension nullable: true, size: 1..16
        phoneNumber size: 1..64

    }

    static mapping = {
        table name: "Telephone"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'Telephone_seq'], sqlType: 'BIGINT'
        telephoneType column: 'telephoneTypeId', sqlType: 'SMALLINT', fetch: FetchMode.JOIN
        person column: Telephone.getUidColumnName(), sqlType: 'VARCHAR(64)'
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        phoneNumber column: 'phoneNumber', sqlType: 'VARCHAR(64)'
        extension column: 'extension', sqlType: 'VARCHAR(16)'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("Telephone", "uid")
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}