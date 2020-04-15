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


import edu.berkeley.bidms.app.matchengine.config.MatchConfidence
import edu.berkeley.bidms.app.matchengine.config.MatchConfig
import edu.berkeley.bidms.app.matchengine.config.MatchReference
import edu.berkeley.bidms.app.matchengine.database.Candidate
import edu.berkeley.bidms.app.matchengine.database.Record
import edu.berkeley.bidms.app.matchengine.service.DatabaseService
import edu.berkeley.bidms.app.matchengine.service.RowMapperService
import edu.berkeley.bidms.app.matchengine.service.SqlService
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Unroll

import static edu.berkeley.bidms.app.matchengine.config.MatchAttributeConfigCreator.create
import static edu.berkeley.bidms.app.matchengine.config.MatchConfig.MatchType.EXACT

@SpringBootTest
class DatabaseServiceIntegrationSpec extends Specification {
    private Sql sqlMock

    @Autowired
    private DatabaseService service

    def setup() {
        service.sqlService = Mock(SqlService)
        service.rowMapperService = Mock(RowMapperService)
        service.matchConfig = new MatchConfig(
                matchTable: 'myMatchTable',
                matchReference: new MatchReference(column: 'reference_id', systemOfRecordAttribute: 'sor', identifierAttribute: 'id'),
                matchAttributeConfigs: [
                        create(name: 'sor', column: 'sorname', isPrimaryKeyColumn: 'isPrimaryKeyColumn', attribute: 'sor'),
                        create(name: 'id', column: "sorobjkey", attribute: 'id'),
                        create(name: 'firstName', column: "FIRST_NAME", path: 'names', attribute: 'given', group: 'official'),
                        create(name: 'lastName', column: "SUR_NAME", path: 'names', attribute: 'sur', group: 'official')
                ],
                canonicalConfidences: [[firstName: EXACT], [lastName: EXACT]].collect { new MatchConfidence(confidence: it, ruleName: 'random') }
        )
        sqlMock = Mock(Sql)
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    @Unroll
    void "test findRecord generates correct SQL and outputs a record"() {
        when:
        def result = service.findRecord("SIS", sorId)

        then:
        1 * service.sqlService.sqlInstance >> sqlMock
        1 * sqlMock.firstRow("SELECT * FROM myMatchTable WHERE sorname=? AND sorobjkey=? AND isPrimaryKeyColumn=?" as String, ['SIS', sorId, true]) >> rowReturned
        result?.referenceId == expectedReferenceId

        where:
        sorId     | rowReturned          | callsToMapper | mappedReturned                   || expectedReferenceId
        "SIS0001" | [reference_id: 'R1'] | 1             | new Candidate(referenceId: 'R1') || 'R1'
        "SIS0002" | null                 | 0             | null                             || null
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    @Unroll
    void "test searchDatabase generates correct SQL and outputs a record"() {
        when:
        def result = service.searchDatabase([names: [[given: 'Kryf', sur: 'Plyf', type: 'official']], sor: 'SIS', id: sorId], ConfidenceType.CANONICAL)

        then:
        2 * service.sqlService.sqlInstance >> sqlMock
        1 * sqlMock.rows('SELECT * FROM myMatchTable WHERE reference_id IS NOT NULL AND lower(FIRST_NAME)=?' as String, ['kryf']) >> rowReturned1
        1 * sqlMock.rows('SELECT * FROM myMatchTable WHERE reference_id IS NOT NULL AND lower(SUR_NAME)=?' as String, ['plyf']) >> rowReturned2
        1 * service.rowMapperService.mapDataRowsToRecords(_, ConfidenceType.CANONICAL, _) >> mappedReturned
        result*.referenceId == expectedReferenceIds

        where:
        sorId     | rowReturned1           | rowReturned2           | callsToMapper | mappedReturned                                                                                     || expectedReferenceIds
        "SIS0001" | [[reference_id: 'R1']] | []                     | 1             | [new Record(referenceId: 'R1', exactMatch: true)]                                                  || ['R1']
        "SIS0002" | [[reference_id: 'R1']] | [[reference_id: 'R2']] | 0             | [new Record(referenceId: 'R1', exactMatch: true), new Record(referenceId: 'R2', exactMatch: true)] || ['R1', 'R2']
        "SIS0030" | []                     | []                     | 0             | []                                                                                                  | []
    }

}