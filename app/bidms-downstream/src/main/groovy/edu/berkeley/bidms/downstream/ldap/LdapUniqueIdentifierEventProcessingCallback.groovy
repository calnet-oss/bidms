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

import edu.berkeley.bidms.app.downstream.service.ldap.LdapDownstreamObjectUpdaterService
import edu.berkeley.bidms.app.downstream.service.ldap.ProvisionLdapServiceCallbackContext
import edu.berkeley.bidms.connector.ldap.event.LdapUniqueIdentifierEventCallback
import edu.berkeley.bidms.connector.ldap.event.message.LdapUniqueIdentifierEventMessage
import org.apache.commons.codec.binary.Hex

class LdapUniqueIdentifierEventProcessingCallback implements LdapUniqueIdentifierEventCallback {
    LdapDownstreamObjectUpdaterService ldapDownstreamObjectUpdaterService

    @Override
    void receive(LdapUniqueIdentifierEventMessage msg) {
        if (msg.success) {
            String globallyUniqueIdentifierString = null

            if (msg.globallyUniqueIdentifier instanceof byte[]) {
                byte[] bytes = (byte[]) msg.globallyUniqueIdentifier

                if (bytes.length == 16) {
                    // 128-bit UUID
                    globallyUniqueIdentifierString = UUID.nameUUIDFromBytes(bytes)
                } else {
                    globallyUniqueIdentifierString = Hex.encodeHexString(bytes)
                }
            } else if (msg.globallyUniqueIdentifier instanceof String) {
                globallyUniqueIdentifierString = (String) msg.globallyUniqueIdentifier
            } else if (msg.globallyUniqueIdentifier) {
                throw new RuntimeException("globallyUniqueIdentifierString can only be a String or a byte[] array")
            }

            ldapDownstreamObjectUpdaterService.updateGloballyUniqueIdentifier(
                    msg.eventId,
                    (ProvisionLdapServiceCallbackContext) msg.context,
                    msg.pkey,
                    msg.oldDn,
                    msg.newDn,
                    msg.wasRenamed,
                    msg.causingEvent,
                    globallyUniqueIdentifierString
            )
        }
    }
}
