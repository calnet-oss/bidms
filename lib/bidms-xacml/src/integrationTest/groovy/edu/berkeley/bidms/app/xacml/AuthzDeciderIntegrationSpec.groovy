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
package edu.berkeley.bidms.app.xacml

import com.att.research.xacml.api.Decision
import com.att.research.xacml.api.Response
import edu.berkeley.bidms.app.registryModel.model.JobAppointment
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.xacml.pep.BidmsXacmlPEPEngine
import edu.berkeley.bidms.xacml.pep.AbstractAuthzDecider
import edu.berkeley.bidms.xacml.request.PersonSubjectResourceRequestImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Service
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AuthzDeciderIntegrationSpec extends Specification {

    @Autowired
    BidmsXacmlPEPEngine pep

    @Autowired
    TestAuthorizationService authService

    @Unroll
    void "test authz check for person-edit"() {
        when: "check authz"
        def result = authService.personEdit(
                new Person(uid: "1").with { person ->
                    addToJobAppointments(new JobAppointment(person).with { appt ->
                        appt.deptCode = deptNoSubject
                        appt
                    })
                },
                new Person(uid: "2").with { person ->
                    addToJobAppointments(new JobAppointment(person).with { appt ->
                        appt.deptCode = deptNoResource
                        appt
                    })
                }
        )

        then:
        result.results[0].decision == exptdDecision

        where:
        deptNoSubject | deptNoResource || exptdDecision
        "DEPT9"       | "DEPT9"        || Decision.PERMIT
        "DEPT8"       | "DEPT9"        || Decision.DENY
        null          | null           || Decision.DENY
        "DEPT8"       | null           || Decision.DENY
        null          | "DEPT9"        || Decision.DENY
    }

    @Service
    static class TestAuthorizationService extends AbstractAuthzDecider {

        TestAuthorizationService(BidmsXacmlPEPEngine pep) {
            super(pep)
        }

        Response personEdit(Person admin, Person user) {
            return decide(new PersonSubjectResourceRequestImpl("person-edit", admin, user))
        }
    }
}
