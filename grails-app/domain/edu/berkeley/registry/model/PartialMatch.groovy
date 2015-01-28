package edu.berkeley.registry.model

class PartialMatch {
    SORObject sorObject
    Person person
    Date dateCreated

    static constraints = {
        sorObject nullable: false
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
