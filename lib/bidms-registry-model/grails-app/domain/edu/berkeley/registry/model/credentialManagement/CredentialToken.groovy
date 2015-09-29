package edu.berkeley.registry.model.credentialManagement

import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.RandomStringUtil

import static edu.berkeley.registry.model.RandomStringUtil.CharTemplate.*

class CredentialToken {
    String token
    Person person
    Date expiryDate
    static constraints = {
        token nullable: false
        person nullable: false
        expiryDate nullable: false
    }

    static mapping = {
        table: "credentialToken"
        version false

        id column: 'id', generator: 'sequence', params: [sequence: 'CredentialToken_seq'], sqlType: 'BIGINT'
        token column: 'token'
        person column: 'personUid'
        expiryDate column: 'expiryDate'
    }

    def beforeInsert() {
        if (!token) {
            token = RandomStringUtil.randomString(10, UPPER_ALPHA, LOWER_ALPHA, NUMERIC)
        }
    }

}
