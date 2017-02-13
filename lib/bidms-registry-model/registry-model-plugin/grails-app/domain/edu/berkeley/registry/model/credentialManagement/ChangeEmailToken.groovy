package edu.berkeley.registry.model.credentialManagement

class ChangeEmailToken extends BaseToken {
    String emailAddress
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
