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

import edu.berkeley.bidms.app.registryModel.model.type.DownstreamSystemEnum
import edu.berkeley.bidms.connector.ldap.UidObjectDefinition
import groovy.transform.InheritConstructors
import org.springframework.ldap.query.LdapQuery
import org.springframework.ldap.query.SearchScope

import static org.springframework.ldap.query.LdapQueryBuilder.query

@InheritConstructors
class MainEntryUidObjectDefinition extends UidObjectDefinition implements SystemUidObjectDefinition {
    private static final String SYSTEM_TYPE = DownstreamSystemEnum.LDAP.name.toLowerCase()

    String searchBase

    static enum SysOp implements SystemOperation {
        ldapDelete, ldapInsert, ldapRename, ldapUpdate

        @Override
        String getSystemType() {
            return SYSTEM_TYPE
        }

        @Override
        Enum getAuditOp() {
            return this
        }

        @Override
        String getSystemTypeAttrName() {
            return "systemName"
        }
    }

    @Override
    String getSystemType() {
        return SYSTEM_TYPE
    }

    @Override
    SystemOperation getDeleteOp() {
        return SysOp.ldapDelete
    }

    @Override
    SystemOperation getInsertOp() {
        return SysOp.ldapInsert
    }

    @Override
    SystemOperation getRenameOp() {
        return SysOp.ldapRename
    }

    @Override
    SystemOperation getUpdateOp() {
        return SysOp.ldapUpdate
    }

    @Override
    LdapQuery getLdapQueryForGloballyUniqueIdentifier(String pkey, Object uniqueIdentifier) {
        return query()
                .base(searchBase)
                .searchScope(SearchScope.SUBTREE)
                .where("objectClass")
                .is(objectClass)
                .and(primaryKeyAttributeName)
                .is(pkey)
                .and(globallyUniqueIdentifierAttributeName)
                .is(uniqueIdentifier.toString())
    }

    @Override
    LdapQuery getLdapQueryForPrimaryKey(String pkey) {
        return query()
                .base(searchBase)
                .searchScope(SearchScope.SUBTREE)
                .where("objectClass")
                .is(objectClass)
                .and(primaryKeyAttributeName)
                .is(pkey)
    }
}
