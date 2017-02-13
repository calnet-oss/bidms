package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import org.hibernate.FetchMode

@ConverterConfig(excludes = ["person"])
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version", "person"])
class IdentifierArchive implements Comparable {

    Long id
    IdentifierType identifierType
    Long originalSorObjectId // SORObject could have been deleted so don't try to join it
    String identifier
    Boolean wasActive
    boolean wasPrimary
    int oldWeight

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['originalSorObjectId', 'identifierType']
        wasActive nullable: true
    }

    static mapping = {
        table name: "IdentifierArchive"
        version false
        id column: 'originalIdentifierId', generator: 'assigned', sqlType: 'BIGINT'
        person column: IdentifierArchive.getUidColumnName(), sqlType: 'VARCHAR(64)'
        identifierType column: 'identifierTypeId', sqlType: 'SMALLINT', fetch: FetchMode.JOIN
        originalSorObjectId column: 'originalSorObjectId', sqlType: 'BIGINT'
        identifier column: 'identifier', sqlType: 'VARCHAR(64)'
        wasActive column: 'wasActive', sqlType: 'BOOLEAN'
        wasPrimary column: 'wasPrimary', sqlType: 'BOOLEAN'
        oldWeight column: 'oldWeight', sqlType: 'INTEGER'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("IdentifierArchive", "uid")
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }

    /**
     * IdentifierArchive is a read-only table updated by DB triggers, but
     * tests may override enforceReadOnly to write mock data.  (Override it
     * to return true.)
     */
    protected boolean enforceReadOnly() {
        throw new RuntimeException('IdentifierArchive is a read-only table')
    }

    def beforeInsert() { return enforceReadOnly() }

    def beforeUpdate() { return enforceReadOnly() }

    // beforeDelete() is not present to allow for deletion of Person objects
}
