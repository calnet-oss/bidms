package edu.berkeley.registry.model

class PartialMatch {
    SORObject sorObject
    Person person
    Date dateCreated = new Date()

    static constraints = {
        // 'unique' GRAILS BUG: UNCOMMENT WHEN FIXED: https://jira.grails.org/browse/GRAILS-11600
        sorObject nullable: false/*, unique: 'person'*/
        person nullable: false
        dateCreated nullable: false
    }

    static mapping = {
        table name: "PartialMatch"
        id sqlType: "BIGINT", generator: 'sequence', params: [sequence: 'sor_seq']
        version false
        sorObject column: 'sorObjectId'
        person column: "personUid"
        dateCreated column: 'dateCreated'
    }
}
