package edu.berkeley.registry.model.credentialManagement

import edu.berkeley.registry.model.IdentifierType
import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.RandomStringUtil
import edu.berkeley.util.domain.DomainUtil
import org.hibernate.FetchMode

import static edu.berkeley.registry.model.RandomStringUtil.CharTemplate.*

class CredentialToken {
    String token
    IdentifierType identifierType
    Person person
    Date expiryDate
    static constraints = {
        token nullable: false, maxSize: 32
        identifierType nullable: false
        person nullable: false, unique: 'identifierType'
        expiryDate nullable: false
    }

    static mapping = {
        table name: "credentialToken"
        version false

        id column: 'id', generator: 'sequence', params: [sequence: 'CredentialToken_seq'], sqlType: 'BIGINT'
        token column: 'token'
        identifierType column: 'identifierTypeId', sqlType: 'SMALLINT', fetch: FetchMode.JOIN
        person column: CredentialToken.getUidColumnName(), sqlType: 'VARCHAR(64)'
        expiryDate column: 'expiryDate'
    }

    def beforeValidate() {
        if (!token) {
            token = RandomStringUtil.randomString(10, UPPER_ALPHA, LOWER_ALPHA, NUMERIC)
        }
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("CredentialToken", "uid")
    }


}
