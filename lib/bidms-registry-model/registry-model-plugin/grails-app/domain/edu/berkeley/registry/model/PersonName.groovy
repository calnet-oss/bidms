package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.hibernate.usertype.JSONBType
import edu.berkeley.util.domain.DomainUtil
import edu.berkeley.util.domain.transform.ConverterConfig
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.hibernate.FetchMode

@ConverterConfig(excludes = ["person", "sorObject", "honorifics"])
@LogicalEqualsAndHashCode(
        excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version", "person", "honorificsAsList"],
        changeCallbackClass = PersonNameHashCodeChangeCallback
)
class PersonName implements Comparable {

    static class PersonNameHashCodeChangeCallback extends PersonCollectionHashCodeChangeHandler<PersonName> {
        PersonNameHashCodeChangeCallback() {
            super("names")
        }
    }

    Long id
    NameType nameType
    SORObject sorObject
    String prefix
    String givenName
    String middleName
    String surName
    String suffix
    String fullName
    String honorifics // This is a JSON array of strings.  Stored in PostGreSQL as JSONB.
    boolean isPrimary

    static transients = ['honorificsAsList']

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['sorObject', 'nameType']
        prefix nullable: true, size: 1..32
        givenName nullable: true, size: 1..127
        middleName nullable: true, size: 1..127
        surName nullable: true, size: 1..127
        suffix nullable: true, size: 1..32
        honorifics nullable: true
        fullName nullable: true, size: 1..1023
    }

    static mapping = {
        table name: "PersonName"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'PersonName_seq'], sqlType: 'BIGINT'
        person column: PersonName.getUidColumnName(), sqlType: 'VARCHAR(64)'
        nameType column: 'nameTypeId', sqlType: 'SMALLINT', fetch: FetchMode.JOIN
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        prefix column: 'prefix', sqlType: 'VARCHAR(32)'
        givenName column: 'givenName', sqlType: 'VARCHAR(127)'
        middleName column: 'middleName', sqlType: 'VARCHAR(127)'
        surName column: 'surName', sqlType: 'VARCHAR(127)'
        suffix column: 'suffix', sqlType: 'VARCHAR(32)'
        honorifics column: 'honorifics', type: JSONBType, sqlType: 'jsonb'
        fullName column: 'fullName', sqlType: 'VARCHAR(255)'
        isPrimary column: 'isPrimary', sqlType: 'BOOLEAN'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("PersonName", "uid")
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }

    List getHonorificsAsList() {
        return (honorifics ? new JsonSlurper().parseText(honorifics) as List : null)
    }

    void setHonorificsAsList(List honorificsAsList) {
        this.honorifics = (honorificsAsList ? new JsonBuilder(honorificsAsList).toString() : null)
    }
}
