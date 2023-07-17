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
package edu.berkeley.bidms.xacml.request

import com.att.research.xacml.std.annotations.XACMLAction
import com.att.research.xacml.std.annotations.XACMLAttribute
import com.att.research.xacml.std.annotations.XACMLRequest
import com.att.research.xacml.std.annotations.XACMLResource
import com.att.research.xacml.std.annotations.XACMLSubject
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.xacml.datatype.BidmsXacmlDataTypePerson
import groovy.transform.CompileStatic

@XACMLRequest(ReturnPolicyIdList = true)
@CompileStatic
class PersonSubjectResourceRequestImpl implements PersonSubjectResourceRequest {

    @XACMLAction
    final String actionId

    /**
     * Subject of the request.  For example, if this is an "admin wants to
     * edit a user" request, the subject is the administrator doing the
     * editing.
     */
    @XACMLSubject(datatype = BidmsXacmlDataTypePerson.ID_DATATYPE_BIDMS_PERSON_STRING)
    Person subject

    @XACMLAttribute(attributeId = SUBJECT_ROLES_ATTRIBUTE_ID, datatype = "http://www.w3.org/2001/XMLSchema#string")
    List<String> subjectRoles

    /**
     * Resource being acted on.  For example, if this is an "admin wants to
     * edit a user" request, the resource is the user-to-be-edited.
     */
    @XACMLResource(datatype = BidmsXacmlDataTypePerson.ID_DATATYPE_BIDMS_PERSON_STRING)
    Person resource

    PersonSubjectResourceRequestImpl(String actionId, Person subject, Person resource) {
        this.actionId = actionId
        this.subject = subject
        this.resource = resource
    }
}
