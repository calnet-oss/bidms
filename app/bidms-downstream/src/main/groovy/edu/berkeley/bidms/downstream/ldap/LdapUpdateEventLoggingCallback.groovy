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

import edu.berkeley.bidms.connector.ldap.event.LdapUpdateEventCallback
import edu.berkeley.bidms.connector.ldap.event.message.LdapUpdateEventMessage
import groovy.transform.CompileStatic

@CompileStatic
class LdapUpdateEventLoggingCallback extends LdapEventLoggingCallback<LdapUpdateEventMessage> implements LdapUpdateEventCallback {
    LdapUpdateEventLoggingCallback(String appName) {
        super(appName)
    }

    Map<String, Object> getExtraAuditAttributes(LdapUpdateEventMessage msg) {
        return [:]
    }

    @Override
    void receive(LdapUpdateEventMessage msg) {
        if (msg.success) {
            logAuditEvent(msg.eventId, msg.objectDef, msg.pkey, ([dn: msg.dn, isModified: (msg.modificationItems?.size()) as boolean] as Map<String, Object>) + getExtraAuditAttributes(msg))
        } else {
            logAuditEvent(msg.eventId, msg.objectDef, msg.pkey, ([dn: msg.dn, exception: msg.exception] as Map<String, Object>) + getExtraAuditAttributes(msg))
        }
    }

    @Override
    Enum getAuditOperation(SystemUidObjectDefinition objectDef) {
        return objectDef.updateOp.auditOp
    }

    @Override
    String getSystemType(SystemUidObjectDefinition objectDef) {
        return objectDef.updateOp.systemType
    }

    @Override
    String getSystemTypeAttrName(SystemUidObjectDefinition objectDef) {
        return objectDef.updateOp.systemTypeAttrName
    }
}
