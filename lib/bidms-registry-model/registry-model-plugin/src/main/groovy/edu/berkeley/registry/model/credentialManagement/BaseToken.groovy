package edu.berkeley.registry.model.credentialManagement

import edu.berkeley.registry.model.Person
import edu.berkeley.registry.model.RandomStringUtil
import edu.berkeley.util.domain.DomainUtil

abstract class BaseToken {
    String token
    Person person
    Date expiryDate

    protected static addBaseConstraints(Object constraintsDelegate) {
        Closure constraintsClone = abstractConstraints.clone() as Closure
        constraintsClone.delegate = constraintsDelegate
        constraintsClone.resolveStrategy = Closure.DELEGATE_FIRST
        constraintsClone.call()
    }

    private static abstractConstraints = {
        token nullable: false, maxSize: 32
        person nullable: false
        expiryDate nullable: false
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName(String className) {
        return DomainUtil.testSafeColumnName(className, "uid")
    }

    protected static addBaseMappings(String className, Object mappingDelegate) {
        Closure mappingClone = abstractMapping.clone() as Closure
        mappingClone.delegate = mappingDelegate
        mappingClone.resolveStrategy = Closure.DELEGATE_FIRST
        mappingClone.call(className)
    }

    private static abstractMapping = { String className ->
        token column: 'token'
        person column: BaseToken.getUidColumnName(className), sqlType: 'VARCHAR(64)'
        expiryDate column: 'expiryDate'
    }

    def beforeValidate() {
        if (!token) {
            token = RandomStringUtil.randomString(20, RandomStringUtil.CharTemplate.UPPER_ALPHA, RandomStringUtil.CharTemplate.LOWER_ALPHA, RandomStringUtil.CharTemplate.NUMERIC)
        }
    }
}
