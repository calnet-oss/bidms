/*
 * Copyright (c) 2014, Regents of the University of California and
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

import edu.berkeley.bidms.app.matchengine.config.MatchConfig
import edu.berkeley.bidms.app.matchengine.config.MatchConfigFactoryBean
import edu.berkeley.bidms.app.matchengine.service.RowMapperService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
class RowMapperServiceIntegrationSpec extends Specification {
    @Autowired
    RowMapperService service

    // First two rows are from different SOR's but same referenceId
    // Last two rows are dupplicate rows for the same person
    @Shared
    List<SearchResult> searchResults = [
            [ruleName: "A", reference_id: 'R1', sor: "X", sorid: "X123", attr_identifier_national: "123-45-6789", attr_name_given_official: "James", attr_name_family_official: "Dean", attr_date_of_birth: "1939-02-08", attr_identifier_sor_employee: "EID-123"],
            [ruleName: "B", reference_id: 'R1', sor: "Y", sorid: "Y123", attr_identifier_national: "123-45-6789", attr_name_given_official: "James", attr_name_family_official: "Dean", attr_date_of_birth: "1939-02-08", attr_identifier_sor_student: "SID-123"],
            [ruleName: "A", reference_id: 'R2', sor: "X", sorid: "X234", attr_name_given_official: "Jack", attr_name_family_official: "Daniels", attr_date_of_birth: "1901-02-01"],
            [ruleName: "C", reference_id: 'R2', sor: "X", sorid: "X234", attr_name_given_official: "Jack", attr_name_family_official: "Daniels", attr_date_of_birth: "1901-02-01"]
    ].collect { new SearchResult(it.remove('ruleName'), [it] as Set<Map<String, Object>>) }

    void "test mapping of single database row to candidates list"() {
        given:
        service.matchConfig = TestMatchConfig.nonInvalidatingConfig

        when:
        def candidates = service.mapDataRowsToRecords(searchResults[0..0], ConfidenceType.CANONICAL, [:]) // Input attributes not important here


        then:
        candidates.size() == 1
        candidates[0].exactMatch == ConfidenceType.CANONICAL.exactMatch
        candidates[0].referenceId == 'R1'
        candidates[0].ruleNames == ['A']
    }

    void "test mapping of multiple database rows to candidates list"() {
        given:
        service.matchConfig = TestMatchConfig.nonInvalidatingConfig

        when:
        def candidates = service.mapDataRowsToRecords(searchResults, ConfidenceType.POTENTIAL, [:]) // Input attributes not important here

        then:
        candidates.size() == 2
        candidates[0].exactMatch == ConfidenceType.POTENTIAL.exactMatch
        candidates[0].referenceId == 'R1'
        candidates[0].ruleNames == ['A', 'B']
        candidates[1].exactMatch == ConfidenceType.POTENTIAL.exactMatch
        candidates[1].referenceId == 'R2'
        candidates[1].ruleNames == ['A', 'C']

    }

    static class TestMatchConfig {
        static MatchConfig getNonInvalidatingConfig() {
            def config = '''
            import static edu.berkeley.bidms.app.matchengine.config.MatchConfig.MatchType.*
            matchTable('myTable')

            referenceId {
                responseType = 'enterprise'
                column = 'reference_id'
                systemOfRecordAttribute = 'identifiersor'
                identifierAttribute = 'identifier'
            }

            attributes {
                identifiersor {
                    description = 'System of Record'
                    column = 'identifiersor'
                    isPrimaryKeyColumn = 'idissorprimarykey'
                    attribute = 'systemOfRecord'
                    search {
                        caseSensitive = true
                    }
                }
                identifier {
                    description = 'System of Record'
                    column = 'identifiersor'
                    isPrimaryKeyColumn = 'idissorprimarykey'
                    attribute = 'systemOfRecord'
                    search {
                        caseSensitive = true
                    }
                }
                socialSecurityNumber {
                    description = 'Social Security Number'
                    column = 'identifier'
                    path = 'identifiers'
                    attribute = 'identifier'
                    group = 'socialSecurityNumber'
                    nullEquivalents = [/[-_]+(?:0{4,5})?(?:9{4,5})?/]  // SSN's with
                    search {
                        alphanumeric = true
                        distance = 2
                    }
                }
                firstname {
                    description = 'Given Name (official)'
                    column = 'attr_name_given_official'
                    path = 'names'
                    attribute = 'given'
                    group = 'official'
                    search {
                        caseSensitive = false
                        distance = 2
                    }
                }
                lastname {
                    description = 'Family Name (official)'
                    column = 'attr_name_family_official'
                    path = 'names'
                    attribute = 'family'
                    group = 'official'
                    search {
                        caseSensitive = false
                        distance = 2
                    }
                }
                dateOfBirth {
                    description = 'Date of Birth'
                    column = 'birthdate'
                    attribute = 'dateOfBirth'
                    invalidates = true
                    search {
                        dateFormat = 'yyyy-MM-dd'
                    }
                }
                employeeid {
                    description = 'Employee ID'
                    column = 'attr_identifier_sor_employee'
                    path = 'identifiers'
                    attribute = 'identifier'
                    group = 'sor-employee'
                    search {
                        caseSensitive = true
                    }
                }
                studentid {
                    description = 'Student ID\'
                    column = 'attr_identifier\'
                    path = 'identifiers\'
                    attribute = 'identifier\'
                    group = 'sor-student\'
                    search {
                        caseSensitive = true
                    }
                }
            }
            confidences {
                canonical identifiersor: EXACT, identifier: EXACT
                canonical socialSecurityNumber: EXACT, firstname: SUBSTRING, lastname: EXACT, dateOfBirth: EXACT
                canonical employeeid: EXACT, firstname: EXACT, lastname: EXACT
                canonical studentid: EXACT, firstname: SUBSTRING, lastname: EXACT

                potential firstname: EXACT, lastname: EXACT, dateOfBirth: DISTANCE
                potential socialSecurityNumber: DISTANCE, firstname: EXACT, lastname: EXACT, dateOfBirth: EXACT
                potential socialSecurityNumber: EXACT, dateOfBirth: EXACT
                potential firstname: DISTANCE, lastname: DISTANCE, dateOfBirth: EXACT
            }
 '''
            MatchConfigFactoryBean.parseConfig(config)
        }
    }

}
