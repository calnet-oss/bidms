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

    @SuppressWarnings("GroovyAssignabilityCheck")
    void "test findRecord within a transaction generates correct calls and sql"() {
        def closure =  {
            findRecord("SIS", "SIS0001", sqlMock)
        }

        when:
        def result = service.withTransaction(closure)

        then:
        1 * service.sqlService.sqlInstance >> sqlMock
        1 * sqlMock.withTransaction(_)  >> { Closure c -> return c.call(sqlMock) }
        1 * sqlMock.firstRow(_) >> [reference_id: 'R1']

        and:
        0 * sqlMock._(*_)

        result.referenceId == 'R1'
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    void "test removeRecord generates correct SQL "() {
        when:
        def result = service.removeRecord("SIS", "SIS0001")

        then:
        1 * service.sqlService.sqlInstance >> sqlMock
        1 * sqlMock.execute("DELETE FROM matchgrid WHERE sor='SIS' AND sorid='SIS0001'") >> true
        result

        and:
        0 * sqlMock._(*_)
    }
}
