package edu.berkeley.registry.model

import edu.berkeley.hibernate.usertype.JSONBType
import edu.berkeley.util.domain.DomainUtil
import grails.converters.JSON
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class PartialMatch {
    SORObject sorObject
    Person person
    Date dateCreated = new Date()
    Boolean isReject = false
    String metaDataJson = '{}'
    Map metaData = [:]

    static transients = ['metaData']

    static constraints = {
        sorObject nullable: false, unique: 'person'
        metaDataJson nullable: true
    }

    static mapping = {
        table name: "PartialMatch"
        id sqlType: "BIGINT", generator: 'sequence', params: [sequence: 'partialmatch_seq']
        version false
        sorObject column: PartialMatch.getSorObjectIdColumnName()
        person column: "personUid"
        dateCreated column: 'dateCreated'
        isReject column: 'isReject'
        metaDataJson column: 'metaDataJson', type: JSONBType, sqlType: 'jsonb'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getSorObjectIdColumnName() {
        return DomainUtil.testSafeColumnName("PartialMatch", "sorObjectId")
    }

    def afterLoad() {
        metaData = new JsonSlurper().parseText(metaDataJson ?: '{}') as Map
    }

    def beforeValidate() {
        metaDataJson = JsonOutput.toJson(metaData ?: [:])
    }
}
