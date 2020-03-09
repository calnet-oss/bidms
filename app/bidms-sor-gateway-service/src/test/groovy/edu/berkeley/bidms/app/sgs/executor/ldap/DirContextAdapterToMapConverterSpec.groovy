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

import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.support.LdapNameBuilder
import spock.lang.Specification

import javax.naming.directory.BasicAttribute

class DirContextAdapterToMapConverterSpec extends Specification {

    def "test conversion of a DirContextAdapter instance to a Map"() {
        given:
        def dnName = LdapNameBuilder.newInstance("uid=1,dc=example,dc=edu").build()
        def dirCtx = new DirContextAdapter(dnName)
        dirCtx.attributes.put("single", "single-value")
        def multiValueAttr = new BasicAttribute("multi")
        multiValueAttr.add("multi-value1")
        multiValueAttr.add("multi-value2")
        dirCtx.attributes.put(multiValueAttr)
        def dirContextAdapterToMapConverter = new DirContextAdapterToMapConverter()

        when:
        def map = dirContextAdapterToMapConverter.convert(dirCtx)

        then:
        map.dn == "uid=1,dc=example,dc=edu"
        map.dnObject == dnName
        map.single == "single-value"
        map.multi == ["multi-value1", "multi-value2"]
    }
}
