package edu.berkeley.registry.model

import edu.berkeley.util.domain.DomainUtil

class PartialMatch {
    SORObject sorObject
    Person person
    Date dateCreated = new Date()

    static constraints = {
        sorObject nullable: false, unique: 'person'
        person nullable: false
        dateCreated nullable: false
    }

    static mapping = {
        table name: "PartialMatch"
        id sqlType: "BIGINT", generator: 'sequence', params: [sequence: 'sor_seq']
        version false
        sorObject column: PartialMatch.getSorObjectIdColumnName()
        person column: "personUid"
        dateCreated column: 'dateCreated'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getSorObjectIdColumnName() {
        return DomainUtil.testSafeColumnName("PartialMatch", "sorObjectId")
    }
}
