package edu.berkeley.registry.model

import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import edu.berkeley.util.domain.transform.LogicalEqualsAndHashCode
import org.hibernate.FetchMode

@ConverterConfig(excludes = ["person", "sorObject"])
@LogicalEqualsAndHashCode(excludes = ["person"])
class Email implements Comparable {
    Long id
    EmailType emailType
    SORObject sorObject
    String emailAddress

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['sorObject', 'emailType', 'emailAddress']
        emailAddress email: true, size: 1..255

    }

    static mapping = {
        table name: "Email"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'Email_seq'], sqlType: 'BIGINT'
        emailType column: 'emailTypeId', sqlType: 'SMALLINT', fetch: FetchMode.JOIN
        person column: Email.getUidColumnName(), sqlType: 'VARCHAR(64)'
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        emailAddress column: 'emailAddress', sqlType: 'VARCHAR(255)'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("Email", "uid")
    }

    int compareTo(obj) {
        return (logicalHashCode() != obj.logicalHashCode() ? logicalHashCode().compareTo(obj.logicalHashCode()) : hashCode().compareTo(obj.hashCode()))
    }
}
