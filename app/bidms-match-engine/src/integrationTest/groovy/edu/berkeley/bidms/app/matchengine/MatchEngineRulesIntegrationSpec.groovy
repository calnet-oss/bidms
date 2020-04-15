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
package edu.berkeley.bidms.app.matchengine

import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

import javax.sql.DataSource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MatchEngineRulesIntegrationSpec extends Specification {

    @LocalServerPort
    int port

    @Autowired
    RestTemplateBuilder restTemplateBuilder

    @Autowired
    DataSource dataSource

    Sql sql
    TestRestTemplate restTemplate

    void setup() {
        this.sql = new Sql(dataSource)
        this.restTemplate = new TestRestTemplate(restTemplateBuilder)
        setupMatchView(sql)
    }

    void cleanup() {
        cleanupMatchView(sql)
        sql.close()
    }

    void setupMatchView(Sql sql) {
        sql.execute("""CREATE TABLE MatchView (
    uid             VARCHAR(64) NOT NULL,
  personNameType    VARCHAR(64),
  personNameSor     VARCHAR(64),
  personNameId      BIGINT,
  fullName          VARCHAR(255),
  givenName         VARCHAR(127),
  middleName        VARCHAR(127),
  surName           VARCHAR(127),
  dobSor            VARCHAR(64),
  dobId             BIGINT,
  birthDate         DATE,
  identifierType    VARCHAR(64) NOT NULL,
  identifierSor     VARCHAR(64),
  identifierId      BIGINT,
  identifier        VARCHAR(64) NOT NULL,
  idIsSorPrimaryKey BOOLEAN
)""" as String)
    }

    void cleanupMatchView(Sql sql) {
        sql.execute("DROP TABLE MatchView" as String)
    }

    def 'test that SOR and primary identifier gives an exact match'() {
        given:
        Map requestData = [
                systemOfRecord: 'PAYROLL',
                identifier    : '300000000'
        ]

        when: "a pre-existing entry with the SOR and primary identifier is created"
        sql.executeInsert(
                "INSERT INTO MatchView (uid, identifierType, identifierSor, identifier, idIsSorPrimaryKey) VALUES(?,?,?,?,?)" as String,
                "20000000", // uid
                "employeeId", // identifierType
                "PAYROLL", // identifierSor
                "300000000", // identifier
                true // idIsSorPrimaryKey
        )

        and: "request match search for same identifier"
        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:${port}/match-engine/person" as String, requestData, Map)

        and: "cleanup"
        sql.executeUpdate("DELETE FROM MatchView WHERE uid=?" as String, "20000000")

        then:
        response.statusCode == HttpStatus.FOUND
        response.body.matchingRecord.referenceId == "20000000"
        response.body.matchingRecord.exactMatch
    }

    def "test that non-student SOR record with student ID gives an exact match"() {
        given:
        Map requestData = [
                systemOfRecord: "ALUMNI",
                identifier    : '5000000',
                dateOfBirth   : '1980-01-01',
                names         : [
                        [type: 'official', givenName: 'John', surName: 'Smith']
                ],
                identifiers   : [
                        [type: 'studentId', identifier: '10000000']
                ]
        ]

        when: "a pre-existing student from student SOR is created"
        sql.executeInsert(
                "INSERT INTO MatchView (uid, identifierType, identifierSor, identifier, idIsSorPrimaryKey, personNameType, personNameSor, givenName, surName) VALUES(?,?,?,?,?,?,?,?,?)" as String,
                "20000000", // uid
                "studentId", // identifierType
                "STUDENT", // identifierSor
                "10000000", // identifier
                true, // idIsSorPrimaryKey
                "studentName", // personNameType
                "STUDENT", // personNameSor
                "John", // givenName
                "Smith" // surName
        )

        and: "request match search for data from a non-student SOR with a student ID"
        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:${port}/match-engine/person" as String, requestData, Map)

        and: "cleanup"
        sql.executeUpdate("DELETE FROM MatchView WHERE uid=?" as String, "20000000")

        then:
        response.statusCode == HttpStatus.OK
        with(response.body.matchingRecord) {
            referenceId == "20000000"
            exactMatch
            ruleNames.size() == 1 && ruleNames.first().startsWith("Canonical")
        }
    }

    def "test a possible match using first character of givenName and date of birth"() {
        given:
        Map requestData = [
                systemOfRecord: "STUDENT",
                identifier    : '5000000',
                dateOfBirth   : '1980-01-01',
                names         : [
                        [
                                type     : 'official',
                                givenName: 'Dave', // different than the existing name but first character is the same
                                surName  : 'Smith'
                        ]
                ],
                identifiers   : [
                        [type: 'studentId', identifier: '10000000']
                ]
        ]

        when:
        sql.executeInsert(
                "INSERT INTO MatchView (uid, identifierType, identifierSor, identifier, idIsSorPrimaryKey, personNameType, personNameSor, givenName, surName, dobSor, birthDate) VALUES(?,?,?,?,?,?,?,?,?,?,?)" as String,
                "20000000", // uid
                "employeeId", // identifierType
                "PAYROLL", // identifierSor
                "60000000", // identifier
                true, // idIsSorPrimaryKey
                "payrollName", // personNameType
                "PAYROLL", // personNameSor
                "David", // givenName
                "Smith", // surName
                "PAYROLL", // dobSor
                "1980-01-01" // birthDate
        )

        and:
        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:${port}/match-engine/person" as String, requestData, Map)

        and: "cleanup"
        sql.executeUpdate("DELETE FROM MatchView WHERE uid=?" as String, "20000000")

        then:
        response.statusCode == HttpStatus.MULTIPLE_CHOICES
        (response.body.partialMatchingRecords as List).size() == 1
        with((response.body.partialMatchingRecords as List).first()) {
            referenceId == "20000000"
            !exactMatch
            ruleNames.size() == 1 && ruleNames.first().startsWith("Potential")
        }
    }

    def "test rules which tests both superCanonical and fixedValue input attribute"() {
        // There's an existing uid from PAYROLL in the test data, so
        // this is a new incoming real time object for the same person that
        // should super match based on the employeeId.
        given:
        Map requestData = [
                systemOfRecord: "PAYROLL_SECONDARY",
                identifier    : '10000000',
                dateOfBirth   : '1980-01-01',
                names         : [
                        [type: 'official', givenName: 'John', surName: 'Smith']
                ],
                identifiers   : [
                        [type: 'employeeId', identifier: '10000000']
                ]
        ]

        when: "a pre-existing employee for primary payroll SOR is created"
        sql.executeInsert(
                "INSERT INTO MatchView (uid, identifierType, identifierSor, identifier, idIsSorPrimaryKey) VALUES(?,?,?,?,?)" as String,
                "20000000",
                "employeeId",
                "PAYROLL",
                "10000000",
                true
        )

        and: "request match search for data from a secondary payroll SOR"
        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:${port}/match-engine/person" as String, requestData, Map)

        and: "cleanup"
        sql.executeUpdate("DELETE FROM MatchView WHERE uid=?" as String, "20000000")

        then:
        response.statusCode == HttpStatus.OK
        with(response.body.matchingRecord) {
            referenceId == "20000000"
            exactMatch
            ruleNames.size() == 1 && ruleNames.first().startsWith("SuperCanonical")
        }
    }
}
