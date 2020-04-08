package edu.berkeley.registry.model

import edu.berkeley.hibernate.usertype.JSONBType

class PersonSorObjectsJson {

    String id // uid
    Date lastUpdated
    String aggregateJson
    String jsonHash
    String provisionedJsonHash
    Date lastProvisioned
    Boolean forceProvision

    static constraints = {
        provisionedJsonHash nullable: true
        lastProvisioned nullable: true
    }

    static mapping = {
        table name: "PersonSorObjectsJson"
        version false

        id column: 'uid', generator: 'assigned', sqlType: 'VARCHAR(64)'
        lastUpdated column: 'lastUpdated'
        aggregateJson column: 'aggregateJson', type: JSONBType, sqlType: 'jsonb'
        jsonHash column: 'jsonHash', sqlType: 'TEXT'
        provisionedJsonHash column: 'provisionedJsonHash', sqlType: 'TEXT'
        lastProvisioned column: 'lastProvisioned'
        forceProvision column: 'forceProvision', sqlType: 'BOOLEAN'
    }

    /**
     * PersonSorObjectsJson is a read-only table, but tests may override
     * enforceReadOnly to write mock data.  (Override it to return true.)
     */
    protected boolean enforceReadOnly() {
        throw new RuntimeException('PersonSorObjectsJson is a read-only table')
    }

    def beforeInsert() { return enforceReadOnly() }

    def beforeUpdate() { return enforceReadOnly() }

    def beforeDelete() { return enforceReadOnly() }
}
