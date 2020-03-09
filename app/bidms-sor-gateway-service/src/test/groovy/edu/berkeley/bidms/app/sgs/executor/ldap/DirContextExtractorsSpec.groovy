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

import edu.berkeley.bidms.app.sgs.config.properties.LdapConnectionConfigProperties
import edu.berkeley.bidms.app.sgs.config.properties.SorConfigProperties
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.support.LdapNameBuilder
import spock.lang.Specification

import javax.naming.Name
import javax.naming.directory.BasicAttribute
import java.sql.Timestamp
import java.time.OffsetDateTime

class DirContextExtractorsSpec extends Specification {

    static final String LDAP_DATE_FORMAT = "yyyyMMddHHmmss'Z'";

    def "test DirContextHashExtractor"() {
        given: "a DirContext"
        def uid = "1"
        def dnName = LdapNameBuilder.newInstance("uid=$uid,dc=example,dc=edu").build()
        def createTimestamp = "20200224160000Z"
        def modifyTimestamp = "20200225160000Z"
        def inputMap = [
                "uid"            : uid,
                "createTimestamp": createTimestamp,
                "modifyTimestamp": modifyTimestamp,
                "givenName"      : "FirstName",
                "sn"             : "LastName"
        ]
        def dirCtx = getDirContext(dnName, inputMap)

        and: "SorConfigProperties"
        def sorConfig = new SorConfigProperties()
        sorConfig.with {
            sorName = "TEST_SOR"
            hashQueryTimestampSupported = true
        }

        and: "a DirContextHashExtractor"
        def dirContextHashExtractor = new DirContextHashExtractor(LdapConnectionConfigProperties.LdapDialect.OTHER, "createTimestamp", "modifyTimestamp")

        when: "hash entry content extracted"
        def now = OffsetDateTime.now()
        LdapHashEntryContent hashEntryContent = dirContextHashExtractor.extractContent(sorConfig, now, dirCtx)
        // timeMarker is the modifyTimestamp if present, otherwise the createTimestamp
        Timestamp expectedTimeMarker = new Timestamp(Date.parse(LDAP_DATE_FORMAT, modifyTimestamp, TimeZone.getTimeZone("UTC")).time)

        then: "hash entry content successfully populated with expected values"
        with(hashEntryContent) {
            sorName == "TEST_SOR"
            sorObjKey == uid
            queryTime == now
            fullIdentifier == dnName.toString()
            timeMarker == expectedTimeMarker
            numericMarker == expectedTimeMarker.time * 1000
        }
        hashEntryContent.hash
    }

    def "test DirContextQueryExtractor"() {
        given: "a DirContext"
        def uid = "1"
        def dnName = LdapNameBuilder.newInstance("uid=$uid,dc=example,dc=edu").build()
        def createTimestamp = "20200224160000Z"
        def modifyTimestamp = "20200225160000Z"
        def inputMap = [
                "uid"            : uid,
                "createTimestamp": createTimestamp,
                "modifyTimestamp": modifyTimestamp,
                "givenName"      : "FirstName",
                "sn"             : "LastName"
        ]
        def dirCtx = getDirContext(dnName, inputMap)

        and: "SorConfigProperties"
        def sorConfig = new SorConfigProperties()
        sorConfig.with {
            sorName = "TEST_SOR"
            hashQueryTimestampSupported = true
        }

        and: "a DirContextQueryExtractor"
        def dirContextQueryExtractor = new DirContextQueryExtractor("createTimestamp", "modifyTimestamp", [:])

        when: "query entry content extracted"
        def now = OffsetDateTime.now()
        LdapQueryEntryContent queryEntryContent = dirContextQueryExtractor.extractContent(sorConfig, now, dirCtx)

        then: "query entry content successfully populated with expected values"
        with(queryEntryContent) {
            sorName == "TEST_SOR"
            sorObjKey == uid
            queryTime == now
            fullIdentifier == dnName.toString()
        }
        queryEntryContent.nativeContent == [
                definitiveUid: uid,
                dn           : dnName.toString(),
                givenName    : "FirstName",
                sn           : "LastName",
                uid          : uid
        ] as TreeMap<String, Object>
        queryEntryContent.objJson == """{"definitiveUid":"$uid","dn":"${dnName.toString()}","givenName":"FirstName","sn":"LastName","uid":"$uid"}"""
    }

    static DirContextAdapter getDirContext(Name dnName, Map<String, Object> attrMap) {
        def dirCtx = new DirContextAdapter(dnName)
        for (def entry : attrMap.entrySet()) {
            if (entry.value instanceof Collection) {
                // multi-value
                def multiValueAttr = new BasicAttribute(entry.key)
                for (def value : (Collection) entry.value) {
                    multiValueAttr.add(value)
                }
                dirCtx.attributes.put(multiValueAttr)
            } else {
                // single-value
                dirCtx.attributes.put(entry.key, entry.value)
            }
        }
        return dirCtx
    }
}
