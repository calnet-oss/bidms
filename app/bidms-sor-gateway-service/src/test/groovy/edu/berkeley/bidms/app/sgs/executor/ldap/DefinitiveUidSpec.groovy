/*
 * Copyright (c) 2020, Regents of the University of California and
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
package edu.berkeley.bidms.app.sgs.executor.ldap

import org.springframework.ldap.support.LdapNameBuilder
import spock.lang.Specification
import spock.lang.Unroll

class DefinitiveUidSpec extends Specification {

    final static String base = "dc=example,dc=edu"

    @Unroll
    def "test determining definitive uid: #description"() {
        given:
        def entry = [
                "dn"      : dn,
                "dnObject": LdapNameBuilder.newInstance(dn).build(),
        ] as LinkedHashMap<String, Object>
        if (entryUid) {
            entry.uid = entryUid
        }

        when:
        DefinitiveUid defUid = DefinitiveUid.getDefinitiveUid(entry)

        then:
        with(defUid) {
            definitiveUid == exptdDefUid
            uidMissingFromDN == exptdUidMissingFromDN
            uidMissingAsAttribute == exptdUidMissingAsAttribute
            uidMultipleAttributeValues == exptdUidMultipleAttributeValues
            uidsDontMatchWithDN == exptdUidsDontMatchWithDN
        }

        where:
        description                             | dn            | entryUid               || exptdDefUid | exptdUidMissingFromDN | exptdUidMissingAsAttribute | exptdUidMultipleAttributeValues | exptdUidsDontMatchWithDN
        "success"                               | "uid=1,$base" | "1"                    || "1"         | false                 | false                      | false                           | false
        "success with dupe but equal uids"      | "uid=1,$base" | ["1", "1"] as String[] || "1"         | false                 | false                      | false                           | false
        "no uid in the DN"                      | base          | "1"                    || null        | true                  | false                      | false                           | false
        "no uid in the entry"                   | "uid=1,$base" | null                   || null        | false                 | true                       | false                           | false
        "mismatched uid in DN and entry"        | "uid=2,$base" | "1"                    || null        | false                 | false                      | false                           | true
        "entry uid is a mismatched multi-value" | "uid=1,$base" | ["1", "2"] as String[] || null        | false                 | false                      | true                            | false
    }
}
