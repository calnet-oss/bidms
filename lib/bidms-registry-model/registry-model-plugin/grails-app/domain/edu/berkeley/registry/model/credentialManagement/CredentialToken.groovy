package edu.berkeley.registry.model.credentialManagement

import edu.berkeley.registry.credentialManagement.RegistrationSource
import edu.berkeley.registry.model.Identifier
import org.hibernate.FetchMode

class CredentialToken extends BaseToken {
    Identifier identifier
    RegistrationSource registrationSource = RegistrationSource.DEFAULT
    static constraints = {
        identifier nullable: false
        person nullable: false, unique: 'identifier'
        registrationSource nullable: false
    }

    static mapping = {
        table name: "credentialToken"
        version false

        id column: 'id', generator: 'sequence', params: [sequence: 'CredentialToken_seq'], sqlType: 'BIGINT'
        identifier column: 'identifierId', fetch: FetchMode.JOIN
        registrationSource column: 'registrationsource', sqlType: 'VARCHAR(64)'
        BaseToken.addBaseMappings(CredentialToken.simpleName, delegate)
    }
}
