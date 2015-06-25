package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.config.MatchReference
import edu.berkeley.calnet.ucbmatch.database.Candidate
import grails.test.mixin.TestFor
import groovy.sql.Sql
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(DatabaseService)
class DatabaseServiceSpec extends Specification {
    private Sql sqlMock

    def setup() {
        service.sqlService = Mock(SqlService)
        service.matchConfig = new MatchConfig(
                matchTable: 'myMatchTable',
                matchReference: new MatchReference(column: 'reference_id',systemOfRecordAttribute: 'sor', identifierAttribute: 'id'),
                matchAttributeConfigs: [new MatchAttributeConfig(name: 'sor', column: 'sorname', isPrimaryKeyColumn: 'isPrimaryKeyColumn'), new MatchAttributeConfig(name: 'id', column: 'sorobjkey')]
        )
        sqlMock = Mock(Sql)
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    @Unroll
    void "test findRecord generates correct SQL and outputs a record"() {
        when:
        def result = service.findRecord("SIS", sorId, [:])

        then:
        1 * service.sqlService.sqlInstance >> sqlMock
        1 * sqlMock.firstRow("SELECT * FROM myMatchTable WHERE sorname=? AND sorobjkey=? AND isPrimaryKeyColumn=?",['SIS',sorId,true]) >> rowReturned
        result?.referenceId == expectedReferenceId



        where:
        sorId     | rowReturned          | callsToMapper | mappedReturned                   || expectedReferenceId
        "SIS0001" | [reference_id: 'R1'] | 1             | new Candidate(referenceId: 'R1') || 'R1'
        "SIS0002" | null                 | 0             | null                             || null
    }
}
