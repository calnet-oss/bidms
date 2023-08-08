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
package edu.berkeley.bidms.app.sgs.executor.ldap.integration

import edu.berkeley.bidms.app.sgs.config.properties.LdapConnectionConfigProperties
import edu.berkeley.bidms.app.sgs.config.properties.SgsConfigProperties
import edu.berkeley.bidms.app.sgs.executor.ldap.LdapHashExecutor
import edu.berkeley.bidms.app.sgs.executor.ldap.LdapTemplateExt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.ldap.core.ContextSource
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.ldap.support.LdapNameBuilder
import io.github.bkoehm.apacheds.embedded.EmbeddedLdapServer
import spock.lang.Shared
import spock.lang.Specification

import javax.naming.directory.Attributes
import javax.naming.directory.BasicAttribute
import javax.naming.directory.BasicAttributes

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LdapIntegrationSpec extends Specification {

    @LocalServerPort
    int port

    @Autowired
    SgsConfigProperties sgsConfig

    @Autowired
    JdbcTemplate registryJdbcTemplate

    @Autowired
    RestTemplateBuilder restTemplateBuilder

    @Shared
    EmbeddedLdapServer embeddedLdapServer = new EmbeddedLdapServer() {
        @Override
        String getBasePartitionName() {
            return "example"
        }

        @Override
        String getBaseStructure() {
            return "dc=example,dc=edu"
        }
    }

    void setup() {
        setupEmbeddedLdapServer()
        IntegrationTestUtil.setupRegistryDatabase(registryJdbcTemplate)
    }

    void cleanup() {
        IntegrationTestUtil.cleanupRegistryDatabase(registryJdbcTemplate)
        cleanupEmbeddedLdapServer()
    }

    void setupEmbeddedLdapServer() {
        embeddedLdapServer.init()

        LdapTemplate ldapTemplate = getLdapTemplate(ldapConnectionConfigProperties)

        // ou=People
        ldapTemplate.bind(LdapNameBuilder.newInstance("ou=People").build(), null, buildAttributes([
                objectClass: ["top", "organizationalUnit"],
                ou         : "People",
                description: "People"
        ]))

        addLdapPeopleEntries()
    }

    void cleanupEmbeddedLdapServer() {
        removeLdapPeopleEntries()
        LdapTemplate ldapTemplate = getLdapTemplate(ldapConnectionConfigProperties)
        ldapTemplate.unbind(LdapNameBuilder.newInstance("ou=People").build())
        embeddedLdapServer.destroy()
    }

    private void addLdapPeopleEntries() {
        LdapTemplate ldapTemplate = getLdapTemplate(ldapConnectionConfigProperties)
        [
                [
                        dn         : "uid=1,ou=People",
                        objectClass: ["top", "person", "organizationalPerson", "inetOrgPerson"],
                        uid        : "1",
                        cn         : "First Last",
                        givenName  : "First",
                        sn         : "Last"
                ],
                [
                        dn         : "uid=2,ou=People",
                        objectClass: ["top", "person", "organizationalPerson", "inetOrgPerson"],
                        uid        : "2",
                        cn         : "First2 Last2",
                        givenName  : "First2",
                        sn         : "Last2"
                ]
        ].each { attrMap ->
            String dn = attrMap.dn
            attrMap.remove("dn")
            ldapTemplate.bind(LdapNameBuilder.newInstance(dn).build(), null, buildAttributes(attrMap))
        }
    }

    private void removeLdapPeopleEntries() {
        LdapTemplate ldapTemplate = getLdapTemplate(ldapConnectionConfigProperties)
        [
                "uid=1,ou=People",
                "uid=2,ou=People"
        ].each { dn ->
            ldapTemplate.unbind(LdapNameBuilder.newInstance(dn).build())
        }
    }

    def "test LDAP hashing and querying via controllers"() {
        given: "a REST template"
        TestRestTemplate restTemplate = new TestRestTemplate(restTemplateBuilder)

        when: "hash performed on the directory via REST"
        def response = restTemplate.exchange(
                "http://localhost:${port}/sgs/hash/LDAP",
                HttpMethod.GET,
                new HttpEntity<Map>(null, null),
                Map
        )
        def hashResult = response.body

        and: "query performed on the directory via REST"
        response = restTemplate.exchange(
                "http://localhost:${port}/sgs/query/LDAP",
                HttpMethod.GET,
                new HttpEntity<Map>(null, null),
                Map
        )
        def queryResult = response.body

        then:
        with(hashResult) {
            sorName == "LDAP"
            hashMode == "FULL"
            successfulHashCount == 2
        }
        with(queryResult) {
            sorName == "LDAP"
            queryMode == "FULL"
            successfulQueryCount == 2
        }
    }

    private LdapConnectionConfigProperties getLdapConnectionConfigProperties() {
        return sgsConfig.connections.ldap.get(sgsConfig.sors.LDAP.connectionName);
    }

    private static ContextSource getContextSource(LdapConnectionConfigProperties ldapConnectionConfig) {
        LdapContextSource cs = new LdapContextSource();
        cs.setUserDn(ldapConnectionConfig.getBindDn());
        cs.setPassword(ldapConnectionConfig.getBindPassword());
        cs.setUrl(ldapConnectionConfig.getUrl());
        cs.setBase(ldapConnectionConfig.getBaseDn());
        cs.setBaseEnvironmentProperties(Map.of("com.sun.jndi.ldap.read.timeout", "3600000")); // 60 minutes - needs to be long for big batch queries
        cs.afterPropertiesSet();
        return cs;
    }

    private static LdapTemplateExt getLdapTemplate(LdapConnectionConfigProperties ldapConnectionConfig) {
        return new LdapTemplateExt(getContextSource(ldapConnectionConfig));
    }

    /**
     * Convert a map to an Attributes object that contains all the keys and
     * values from the map.
     */
    private static Attributes buildAttributes(Map<String, Object> attrMap) {
        Attributes attrs = new BasicAttributes()
        attrMap.each { entry ->
            if (entry.value instanceof List) {
                if (((List) entry.value).size()) {
                    BasicAttribute attr = new BasicAttribute(entry.key)
                    entry.value.each {
                        attr.add(it)
                    }
                    attrs.put(attr)
                } else {
                    attrs.remove(entry.key)
                }
            } else if (entry.value != null) {
                attrs.put(entry.key, entry.value)
            } else {
                attrs.remove(entry.key)
            }
        }
        return attrs
    }
}
