package edu.berkeley.registry.model.credentialManagement

import edu.berkeley.registry.model.Person

class ResetPassphraseToken extends BaseToken {
    static belongsTo = [person: Person]

    static constraints = {
        BaseToken.addBaseConstraints(delegate)
    }

    static mapping = {
        table name: "resetPassphraseToken"
        version false

        id column: 'id', generator: 'sequence', params: [sequence: 'ResetPassphraseToken_seq'], sqlType: 'BIGINT'
        BaseToken.addBaseMappings(ResetPassphraseToken.simpleName, delegate)
    }
}
