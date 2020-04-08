package edu.berkeley.registry.model.credentialManagement

import edu.berkeley.registry.model.Person

class ChangeEmailToken extends BaseToken {
    String emailAddress

    static belongsTo = [person: Person]

    static constraints = {
        emailAddress nullable: false, email: true
        BaseToken.addBaseConstraints(delegate)
    }

    static mapping = {
        table name: "changeEmailToken"
        version false

        id column: 'id', generator: 'sequence', params: [sequence: 'ChangeEmailToken_seq'], sqlType: 'BIGINT'
        emailAddress column: 'emailAddress', sqlType: 'VARCHAR(255)'
        BaseToken.addBaseMappings(ChangeEmailToken.simpleName, delegate)
    }
}
