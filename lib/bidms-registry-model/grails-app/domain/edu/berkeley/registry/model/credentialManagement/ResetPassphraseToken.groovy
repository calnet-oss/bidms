package edu.berkeley.registry.model.credentialManagement

import edu.berkeley.registry.model.Identifier
import org.hibernate.FetchMode

class ResetPassphraseToken extends BaseToken {
    Identifier identifier

    static constraints = {
        identifier nullable: false
    }

    static mapping = {
        table name: "resetPassphraseToken"
        version false

        id column: 'id', generator: 'sequence', params: [sequence: 'ResetPassphraseToken_seq'], sqlType: 'BIGINT'
        identifier column: 'identifierId', fetch: FetchMode.JOIN
        BaseToken.addBaseMappings(ResetPassphraseToken.simpleName, delegate)
    }
}
