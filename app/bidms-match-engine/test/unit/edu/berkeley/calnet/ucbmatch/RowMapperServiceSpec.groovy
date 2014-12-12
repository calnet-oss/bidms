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
            [referenceId: 'R1', sor: "X", sorid: "X123", attr_identifier_national: "123-45-6789", attr_name_given_official: "James", attr_name_family_official: "Dean", attr_date_of_birth: "1939-02-08", attr_identifier_sor_employee: "EID-123"],
            [referenceId: 'R1', sor: "Y", sorid: "Y123", attr_identifier_national: "123-45-6789", attr_name_given_official: "James", attr_name_family_official: "Dean", attr_date_of_birth: "1939-02-08", attr_identifier_sor_student: "SID-123"],
            [referenceId: 'R2', sor: "X", sorid: "X234", attr_name_given_official: "Jack", attr_name_family_official: "Daniels", attr_date_of_birth: "1901-02-01"],
            [referenceId: 'R2', sor: "X", sorid: "X234", attr_name_given_official: "Jack", attr_name_family_official: "Daniels", attr_date_of_birth: "1901-02-01"]
    ]
    def setup() {
        service.matchConfig = TestMatchConfig.config
    }

    void "test mapping of single database row to candidates list"() {
        when:
        def candidates = service.mapDataRowsToCandidates(dataRows[0..0] as Set, ConfidenceType.CANONICAL)

        then:
        candidates.size() == 1
        candidates[0].referenceId == 'R1'
        candidates[0].systemOfRecord == 'X'
        candidates[0].names.size() == 1
        candidates[0].dateOfBirth == '1939-02-08'
        candidates[0].names[0].type == 'official'
        candidates[0].names[0].given == 'James'
        candidates[0].names[0].family == 'Dean'
        candidates[0].identifiers.size() == 3
        candidates[0].identifiers[0].type == 'sor'
        candidates[0].identifiers[0].identifier == 'X123'
        candidates[0].identifiers[1].type == 'national'
        candidates[0].identifiers[1].identifier == '123-45-6789'
        candidates[0].identifiers[2].type == 'sor-employee'
        candidates[0].identifiers[2].identifier == 'EID-123'
    }

    void "test mapping of multiple database rows to candidates list"() {
        when:
        def candidates = service.mapDataRowsToCandidates(dataRows as Set, ConfidenceType.CANONICAL)

        then:
        candidates.size() == 3
        candidates[0].referenceId == 'R1'
        candidates[0].systemOfRecord == 'X'
        candidates[0].names.size() == 1
        candidates[0].names[0].type == 'official'
        candidates[0].names[0].given == 'James'
        candidates[0].names[0].family == 'Dean'
        candidates[0].dateOfBirth == '1939-02-08'
        candidates[0].identifiers.size() == 3
        candidates[0].identifiers[0].type == 'sor'
        candidates[0].identifiers[0].identifier == 'X123'
        candidates[0].identifiers[1].type == 'national'
        candidates[0].identifiers[1].identifier == '123-45-6789'
        candidates[0].identifiers[2].type == 'sor-employee'
        candidates[0].identifiers[2].identifier == 'EID-123'
        candidates[1].referenceId == 'R1'
        candidates[1].systemOfRecord == 'Y'
        candidates[1].dateOfBirth == '1939-02-08'
        candidates[1].names.size() == 1
        candidates[1].names[0].type == 'official'
        candidates[1].names[0].given == 'James'
        candidates[1].names[0].family == 'Dean'
        candidates[1].identifiers.size() == 3
        candidates[1].identifiers[0].type == 'sor'
        candidates[1].identifiers[0].identifier == 'Y123'
        candidates[1].identifiers[1].type == 'national'
        candidates[1].identifiers[1].identifier == '123-45-6789'
        candidates[1].identifiers[2].type == 'sor-student'
        candidates[1].identifiers[2].identifier == 'SID-123'
        candidates[2].referenceId == 'R2'
        candidates[2].systemOfRecord == 'X'
        candidates[2].dateOfBirth == '1901-02-01'
        candidates[2].names.size() == 1
        candidates[2].names[0].type == 'official'
        candidates[2].names[0].given == 'Jack'
        candidates[2].names[0].family == 'Daniels'
        candidates[2].identifiers.size() == 1
        candidates[2].identifiers[0].type == 'sor'
        candidates[2].identifiers[0].identifier == 'X234'
    }
}
