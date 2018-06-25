package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.config.MatchConfidence
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.config.MatchReference
import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.database.Record
import grails.testing.services.ServiceUnitTest
import groovy.sql.Sql
import spock.lang.Specification
import spock.lang.Unroll

import static edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig.create
import static edu.berkeley.calnet.ucbmatch.config.MatchConfig.MatchType.EXACT

class DatabaseServiceSpec extends Specification implements ServiceUnitTest<DatabaseService> {
    private Sql sqlMock

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
        1 * sqlMock.firstRow("SELECT * FROM myMatchTable WHERE sorname=? AND sorobjkey=? AND isPrimaryKeyColumn=?", ['SIS', sorId, true]) >> rowReturned
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
        1 * sqlMock.rows('SELECT * FROM myMatchTable WHERE reference_id IS NOT NULL AND lower(FIRST_NAME)=?', ['kryf']) >> rowReturned1
        1 * sqlMock.rows('SELECT * FROM myMatchTable WHERE reference_id IS NOT NULL AND lower(SUR_NAME)=?', ['plyf']) >> rowReturned2
        1 * service.rowMapperService.mapDataRowsToRecords(_, ConfidenceType.CANONICAL, _) >> mappedReturned
        result*.referenceId == expectedReferenceIds

        where:
        sorId     | rowReturned1           | rowReturned2           | callsToMapper | mappedReturned                                                                                     || expectedReferenceIds
        "SIS0001" | [[reference_id: 'R1']] | []                     | 1             | [new Record(referenceId: 'R1', exactMatch: true)]                                                  || ['R1']
        "SIS0002" | [[reference_id: 'R1']] | [[reference_id: 'R2']] | 0             | [new Record(referenceId: 'R1', exactMatch: true), new Record(referenceId: 'R2', exactMatch: true)] || ['R1', 'R2']
        "SIS0030" | []                     | []                     | 0             | []                                                                                                  | []
    }

}
