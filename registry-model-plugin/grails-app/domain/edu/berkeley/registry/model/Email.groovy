package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import org.hibernate.FetchMode

@ConverterConfig(excludes = ["person", "sorObject", "emailAddressLowerCase"])
@LogicalEqualsAndHashCode(excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version", "person", "emailAddressLowerCase"])
class Email implements Comparable {
    Long id
    EmailType emailType
    SORObject sorObject
    String emailAddress
    String emailAddressLowerCase

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['sorObject', 'emailType', 'emailAddress']
        emailAddress nullable: true, size: 1..255
        emailAddressLowerCase nullable: true
    }

    static mapping = {
        table name: "Email"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'Email_seq'], sqlType: 'BIGINT'
        emailType column: 'emailTypeId', sqlType: 'SMALLINT', fetch: FetchMode.JOIN
        person column: Email.getUidColumnName(), sqlType: 'VARCHAR(64)'
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        emailAddress column: 'emailAddress', sqlType: 'VARCHAR(255)'
        emailAddressLowerCase formula: 'LOWER(emailAddress)'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("Email", "uid")
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }

    String getEmailAddressLowerCase() {
        return emailAddress?.toLowerCase()
    }

    void setEmailAddressLowerCase() {
        throw new UnsupportedOperationException("emailAddressLowerCase is read-only.  Use setEmailAddress() instead.")
    }
}
