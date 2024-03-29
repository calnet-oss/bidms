/*
 * Copyright (c) 2017, Regents of the University of California and
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
package edu.berkeley.bidms.downstream.ldap

import edu.berkeley.bidms.connector.ldap.LdapObjectDefinition
import edu.berkeley.bidms.connector.ldap.event.LdapEventCallback
import edu.berkeley.bidms.connector.ldap.event.message.LdapEventMessage
import edu.berkeley.bidms.logging.AuditFailEvent
import edu.berkeley.bidms.logging.AuditSuccessEvent
import edu.berkeley.bidms.logging.AuditUtil
import groovy.transform.CompileStatic

@CompileStatic
abstract class LdapEventLoggingCallback<T extends LdapEventMessage> implements LdapEventCallback<T> {
    private final String appName

    LdapEventLoggingCallback(String appName) {
        this.appName = appName
    }

    protected String getAppName() {
        return appName
    }

    abstract Enum getAuditOperation(SystemUidObjectDefinition objectDef)

    abstract String getSystemType(SystemUidObjectDefinition objectDef)

    abstract String getSystemTypeAttrName(SystemUidObjectDefinition objectDef)

    void logAuditEvent(String eventId, LdapObjectDefinition objectDef, String uid, Map<String, Object> attrs) {
        Throwable exception = (Throwable) attrs.remove("exception")
        if (!exception) {
            AuditUtil.logAuditEvent(appName, new AuditSuccessEvent(eventId: eventId,
                    op: getAuditOperation((SystemUidObjectDefinition) objectDef),
                    forUid: uid,
                    attrs: generateAuditAttrs(objectDef, attrs)),
                    false)
        } else {
            AuditUtil.logAuditEvent(appName, new AuditFailEvent(eventId: eventId,
                    op: getAuditOperation((SystemUidObjectDefinition) objectDef),
                    forUid: uid,
                    errorMsg: exception.getMessage(),
                    attrs: generateAuditAttrs(objectDef, attrs)),
                    false)
        }
    }

    Map<String, Object> generateAuditAttrs(LdapObjectDefinition objectDef, Map<String, Object> passedInAuditAttrs) {
        Map<String, Object> result = [:]
        result[getSystemTypeAttrName((SystemUidObjectDefinition) objectDef)] = getSystemType((SystemUidObjectDefinition) objectDef)
        return result + passedInAuditAttrs
    }
}
