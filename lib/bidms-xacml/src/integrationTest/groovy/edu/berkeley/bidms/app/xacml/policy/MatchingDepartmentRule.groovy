/*
 * Copyright (c) 2023, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.bidms.app.xacml.policy

import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.xacml.pdp.PersonSubjectResourceEvaluator
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class MatchingDepartmentRule implements PersonSubjectResourceEvaluator {

    static final String RULE_ID = "urn:edu:berkeley:bidms:xacml:rule:permit-matched-department"

    static List<String> findMatchingDeptCodes(Person subjectAdmin, Person resourceUser) {
        def userDeptCodes = resourceUser.jobAppointments?.collect { it.deptCode }
        def adminDeptCodes = subjectAdmin.jobAppointments?.collect { it.deptCode }
        return adminDeptCodes?.findAll { adminDeptCode ->
            adminDeptCode && adminDeptCode in userDeptCodes
        }
    }

    @Override
    boolean evaluate(Person subjectAdmin, List<String> subjectRoles, Person resourceUser) {
        def matchingDeptCodes = findMatchingDeptCodes(subjectAdmin, resourceUser)
        if (matchingDeptCodes) {
            log.info("Admin ${subjectAdmin.uid} and user ${resourceUser.uid} share department codes $matchingDeptCodes")
        } else {
            log.info("Admin ${subjectAdmin.uid} and user ${resourceUser.uid} share no departments")
        }
        return matchingDeptCodes
    }
}
