package edu.berkeley.calnet.ucbmatch

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
        sqlMock = Mock(Sql)
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    @Unroll
    void "test findRecord generates correct SQL and outputs a record"() {
        when:
        def result = service.findRecord("SIS", sorId)

        then:
        1 * service.sqlService.sqlInstance >> sqlMock
        1 * sqlMock.firstRow("SELECT * FROM matchgrid WHERE sor='SIS' AND sorid='${sorId}'") >> rowReturned
        result?.referenceId == expectedReferenceId

        and:
        0 * sqlMock._(*_)


        where:
        sorId     | rowReturned           | expectedReferenceId
        "SIS0001" | [reference_id: 'R1'] || 'R1'
        "SIS0002" | null                 || null
    }
}
