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
package edu.berkeley.bidms.app.downstream.service.ldap

import edu.berkeley.bidms.connector.ldap.event.LdapEventType
import edu.berkeley.bidms.logging.AuditSuccessEvent
import edu.berkeley.bidms.logging.AuditUtil
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.sql.DataSource

@Slf4j
@Service
class LdapDownstreamObjectUpdaterService {
    @Value('${bidms.downstream.app-name}')
    private String APP_NAME

    static enum AuditOperation {
        ldapUpdateGloballyUniqueIdentifier
    }

    DataSource dataSource

    LdapDownstreamObjectUpdaterService(DataSource dataSource) {
        this.dataSource = dataSource
    }

    void updateGloballyUniqueIdentifier(
            String eventId,
            ProvisionLdapServiceCallbackContext context,
            String uid,
            String oldDn,
            String newDn,
            boolean wasRenamed,
            LdapEventType causingEvent,
            String globallyUniqueIdentifier
    ) {
        if (!globallyUniqueIdentifier) {
            return
        }

        Sql sql = new Sql(dataSource)
        try {
            String currentGlobUniqId = getCurrentGloballyUniqueIdentifier(sql, context.downstreamSystemId, uid)
            if (globallyUniqueIdentifier != currentGlobUniqId) {
                synchronized (context.lock) {
                    updateDownstreamObject(sql, context.downstreamSystemId, uid, globallyUniqueIdentifier)
                }
                Map attrs = [
                        systemName   : context.systemTypeName,
                        causingEvent : causingEvent.name(),
                        oldGlobUniqId: currentGlobUniqId,
                        newGlobUniqId: globallyUniqueIdentifier
                ]
                if (wasRenamed) {
                    attrs.oldDn = oldDn
                    attrs.newDn = newDn
                } else if (causingEvent == LdapEventType.INSERT_EVENT) {
                    attrs.newDn = newDn
                }
                AuditUtil.logAuditEvent(APP_NAME, new AuditSuccessEvent(
                        eventId: eventId,
                        op: AuditOperation.ldapUpdateGloballyUniqueIdentifier,
                        forUid: uid,
                        attrs: attrs
                ), false)
            } else {
                log.debug("LdapUniqueIdentifierEventProcessingCallback called for systemName=${context.systemTypeName}, uid=$uid, causingEvent=$causingEvent but the globally unique identifier hasn't actually changed")
            }
        }
        finally {
            sql.close()
        }
    }

    String getCurrentGloballyUniqueIdentifier(Sql sql, int downstreamSystemId, String uid) {
        return sql.firstRow("SELECT globUniqId FROM DownstreamObject WHERE systemId = ? AND sysObjKey = ?" as String, [downstreamSystemId, uid])?.globUniqId
    }

    void updateDownstreamObject(Sql sql, int downstreamSystemId, String uid, String globallyUniqueIdentifier) {
        if (sql.executeUpdate("UPDATE DownstreamObject SET globUniqId=? WHERE systemId = ? AND sysObjKey = ?" as String, [globallyUniqueIdentifier, downstreamSystemId, uid]) != 1) {
            log.warn("Couldn't find DownstreamObject for uid $uid, downstreamSystemId=$downstreamSystemId while updating the globally unique identifier")
        }
    }
}
