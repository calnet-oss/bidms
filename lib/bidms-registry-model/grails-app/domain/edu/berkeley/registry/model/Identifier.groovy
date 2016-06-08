package edu.berkeley.registry.model

import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import org.hibernate.FetchMode

@ConverterConfig(excludes = ["person", "sorObject"])
// isPrimary is only excluded because of the order-of-operations in
// registry-provisioning-scripts.  The isPrimary is set later in the
// process, and if isPrimary is not excluded from logicalEquals(), then an
// unchanged primary identifier is at risk of being deleted/readded with a
// new Identifier.id every time the person is reprovisioned, which is not
// what we want.
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "person", "isPrimary"])
class Identifier implements Comparable {

    Long id
    IdentifierType identifierType
    SORObject sorObject
    String identifier
    // indicates if this an active id in the SOR
    Boolean isActive
    // If the identifier is primary for the identifier type
    boolean isPrimary
    /**
     * weight: lowest number (0) is highest priority identifier of the same
     * type.  Used by PrimaryIdentifierExecutor to aid in determining
     * isPrimary.  HrmsIdentifierBuilder is an example where the weight is
     * set to something non-zero.  Defaults to 0.
     */
    int weight

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['sorObject', 'identifierType']
        isActive nullable: true
    }

    static mapping = {
        table name: "Identifier"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'Identifier_seq'], sqlType: 'BIGINT'
        person column: Identifier.getUidColumnName(), sqlType: 'VARCHAR(64)'
        identifierType column: 'identifierTypeId', sqlType: 'SMALLINT', fetch: FetchMode.JOIN
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        identifier column: 'identifier', sqlType: 'VARCHAR(64)'
        isActive column: 'isActive', sqlType: 'BOOLEAN'
        isPrimary column: 'isPrimary', sqlType: 'BOOLEAN'
        weight column: 'weight', sqlType: 'INTEGER', defaultValue: 0
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("Identifier", "uid")
    }

    int compareTo(obj) {
        return logicalHashCode() <=> obj.logicalHashCode() ?: hashCode() <=> obj.hashCode()
    }
}
