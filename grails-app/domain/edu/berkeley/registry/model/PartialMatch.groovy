package edu.berkeley.registry.model

import edu.berkeley.util.domain.DomainUtil

class PartialMatch {
    SORObject sorObject
    Person person
    Date dateCreated = new Date()
    Boolean isReject = false
    String matchRuleString

    void setMatchRules(List<String> matchRules) {
        matchRuleString = matchRules?.join('|') ?: ''
    }

    List<String> getMatchRules() {
        matchRuleString ? matchRuleString.tokenize('|').toList() : []
    }

    static transients = ['matchRules']

    static constraints = {
        sorObject nullable: false, unique: 'person'
        matchRuleString nullable: true, maxSize: 255
    }

    static mapping = {
        table name: "PartialMatch"
        id sqlType: "BIGINT", generator: 'sequence', params: [sequence: 'partialmatch_seq']
        version false
        sorObject column: PartialMatch.getSorObjectIdColumnName()
        person column: "personUid"
        dateCreated column: 'dateCreated'
        isReject column: 'isReject'
        matchRuleString column: 'matchRules'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getSorObjectIdColumnName() {
        return DomainUtil.testSafeColumnName("PartialMatch", "sorObjectId")
    }
}
