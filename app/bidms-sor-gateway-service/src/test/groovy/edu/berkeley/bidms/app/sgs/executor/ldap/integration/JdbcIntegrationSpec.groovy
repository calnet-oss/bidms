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

import edu.berkeley.bidms.app.sgs.config.properties.SgsConfigProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

import static IntegrationTestUtil.SqlStmt

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JdbcIntegrationSpec extends Specification {

    @LocalServerPort
    int port

    @Autowired
    SgsConfigProperties sgsConfig

    @Autowired
    JdbcTemplate registryJdbcTemplate

    @Autowired
    RestTemplateBuilder restTemplateBuilder

    void setup() {
        IntegrationTestUtil.setupRegistryDatabase(registryJdbcTemplate)

        // This is a test Alumni data table that will be hashed and queried
        [
                SqlStmt.create("""CREATE TABLE Alumni (
                    sorObjKey VARCHAR(32) PRIMARY KEY,
                    timeMarker TIMESTAMP NOT NULL,
                    givenName VARCHAR(64),
                    sn VARCHAR(64)
                  )"""
                ),
                SqlStmt.create("INSERT INTO Alumni(sorObjKey, timeMarker, givenName, sn) VALUES(?,CURRENT_TIMESTAMP,?,?)", "t1", "First", "Last"),
                SqlStmt.create("INSERT INTO Alumni(sorObjKey, timeMarker, givenName, sn) VALUES(?,CURRENT_TIMESTAMP,?,?)", "t2", "First2", "Last2")
        ].each { stmt ->
            if (stmt.args) {
                registryJdbcTemplate.update(stmt.sql, stmt.args)
            } else {
                registryJdbcTemplate.execute(stmt.sql)
            }
        }

        File sqlDir = new File(new URL(sgsConfig.sqlTemplateDirectory).file, "sql")
        sqlDir.mkdirs()
        sqlDir.deleteOnExit()

        File hashSqlFile = new File(sqlDir, "alumni-hash.sql")
        hashSqlFile.write('''SELECT sorObjKey, hash, timeMarker FROM (SELECT sorObjKey, ORA_HASH(givenName || sn) AS hash, timeMarker FROM Alumni) ${timeMarkerWhereClause!''}''')

        File querySqlFile = new File(sqlDir, "alumni-query.sql")
        querySqlFile.write('''SELECT sorObjKey, obj, timeMarker FROM (SELECT sorObjKey, '<QUERY><SORNAME>ALUMNI</SORNAME><VERSION>1</VERSION><QUERYTIME>2020-02-27 12:10:00.000 -0800</QUERYTIME><SOROBJKEY>' || sorObjKey || '</SOROBJKEY><ALUMNI><PK>' || sorObjKey || '</PK><GIVENNAME>' || givenName || '</GIVENNAME><SN>' || sn || '</SN></ALUMNI></QUERY>' AS obj, timeMarker FROM Alumni) ${individualWhereClause!''}''')
    }

    void cleanup() {
        [
                SqlStmt.create("DROP TABLE Alumni")
        ].each { stmt ->
            registryJdbcTemplate.execute(stmt.sql)
        }

        IntegrationTestUtil.cleanupRegistryDatabase(registryJdbcTemplate)

        File sqlDir = new File(new URL(sgsConfig.sqlTemplateDirectory).file, "sql")
        sqlDir.delete()
    }

    def "test JDBC hashing and querying via controllers"() {
        given: "a REST template"
        TestRestTemplate restTemplate = new TestRestTemplate(restTemplateBuilder)

        when: "hash performed on the Alumni table via REST"
        def response = restTemplate.exchange(
                "http://localhost:${port}/sgs/hash/ALUMNI",
                HttpMethod.GET,
                new HttpEntity<Map>(null, null),
                Map
        )
        def hashResult = response.body

        and: "query performed on the Alumni via REST"
        response = restTemplate.exchange(
                "http://localhost:${port}/sgs/query/ALUMNI",
                HttpMethod.GET,
                new HttpEntity<Map>(null, null),
                Map
        )
        def queryResult = response.body

        then:
        with(hashResult) {
            sorName == "ALUMNI"
            hashMode == "FULL"
            successfulHashCount == 2
        }
        with(queryResult) {
            sorName == "ALUMNI"
            queryMode == "FULL"
            successfulQueryCount == 2
        }
    }
}
