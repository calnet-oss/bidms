package edu.berkeley.registry.model.credentialManagement

class ResetPassphraseToken extends BaseToken {
    static constraints = {
    }

    static mapping = {
        table name: "resetPassphraseToken"
        version false

        id column: 'id', generator: 'sequence', params: [sequence: 'ResetPassphraseToken_seq'], sqlType: 'BIGINT'
        BaseToken.addBaseMappings(ResetPassphraseToken.simpleName, delegate)
    }
}
