package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.config.TestMatchConfig
import grails.test.mixin.TestFor
import spock.lang.Shared
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(RowMapperService)
class RowMapperServiceSpec extends Specification {
    /**
     * First two rows are from different SOR's but same referenceId
     * Last two rows are dupplicate rows for the same person
     */
    @Shared def dataRows = [
            [reference_id: 'R1', sor: "X", sorid: "X123", attr_identifier_national: "123-45-6789", attr_name_given_official: "James", attr_name_family_official: "Dean", attr_date_of_birth: "1939-02-08", attr_identifier_sor_employee: "EID-123"],
            [reference_id: 'R1', sor: "Y", sorid: "Y123", attr_identifier_national: "123-45-6789", attr_name_given_official: "James", attr_name_family_official: "Dean", attr_date_of_birth: "1939-02-08", attr_identifier_sor_student: "SID-123"],
            [reference_id: 'R2', sor: "X", sorid: "X234", attr_name_given_official: "Jack", attr_name_family_official: "Daniels", attr_date_of_birth: "1901-02-01"],
            [reference_id: 'R2', sor: "X", sorid: "X234", attr_name_given_official: "Jack", attr_name_family_official: "Daniels", attr_date_of_birth: "1901-02-01"]
    ]
    def setup() {
    }

    void "test mapping of single database row to candidates list"() {
        given:
        service.matchConfig = TestMatchConfig.nonInvalidatingConfig


        when:
        def candidates = service.mapDataRowsToRecords(dataRows[0..0] as Set, ConfidenceType.CANONICAL, [:]) // Input attributes not important here


        then:
        candidates.size() == 1
        candidates[0].exactMatch == ConfidenceType.CANONICAL.exactMatch
        candidates[0].referenceId == 'R1'
    }

    void "test mapping of multiple database rows to candidates list"() {
        given:
        service.matchConfig = TestMatchConfig.nonInvalidatingConfig

        when:
        def candidates = service.mapDataRowsToRecords(dataRows as Set, ConfidenceType.POTENTIAL, [:]) // Input attributes not important here

        then:
        candidates.size() == 2
        candidates[0].exactMatch == ConfidenceType.POTENTIAL.exactMatch
        candidates[0].referenceId == 'R1'
    }
}
