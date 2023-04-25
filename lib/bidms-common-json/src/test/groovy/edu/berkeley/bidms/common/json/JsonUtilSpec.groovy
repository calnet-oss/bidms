package edu.berkeley.bidms.common.json

import spock.lang.Specification
import spock.lang.Unroll

class JsonUtilSpec extends Specification {

    @Unroll
    void "test JsonUtil.convertXmlToJson: #description"() {
        when:
        String result = JsonUtil.convertXmlToJson(withSpacing ? xmlMsg : xmlMsg.replaceAll('[ \\n]', ''))

        then:
        result == exptdJson

        where:
        description                      | xmlMsg          | withSpacing || exptdJson
        "single name without spacing"    | SINGLE_NAME_XML | false       || EXPTD_SINGLE_NAME_JSON
        "multiple names without spacing" | MULTI_NAME_XML  | false       || EXPTD_MULTI_NAME_JSON
        "single name with spacing"       | SINGLE_NAME_XML | true        || EXPTD_SINGLE_NAME_JSON
        "multiple names with spacing"    | MULTI_NAME_XML  | true        || EXPTD_MULTI_NAME_JSON
    }

    static final String EXPTD_SINGLE_NAME_JSON = '{"MSG":{"PERSON":{"ID":"1","NAMES":{"NAME":{"FIRST_NAME":"First1","LAST_NAME":"Last1","ALT_NAMES":[],"TYPES":[{"CODE":"PRI"},{"CODE":"PRF"}]}},"GROUPS":[]}}}'
    static final String EXPTD_MULTI_NAME_JSON = '{"MSG":{"PERSON":{"ID":"1","NAMES":[{"FIRST_NAME":"First1","LAST_NAME":"Last1","ALT_NAMES":[],"TYPES":{"TYPE":{"CODE":"PRI"}}},{"FIRST_NAME":"First2","LAST_NAME":"Last2","ALT_NAMES":[],"TYPES":[{"CODE":"PRI"},{"CODE":"PRF"}]}],"GROUPS":[]}}}'

    static final String SINGLE_NAME_XML = '''<MSG>
    <PERSON>
        <ID>1</ID>
        <NAMES>
            <NAME>
                <FIRST_NAME>First1</FIRST_NAME>
                <LAST_NAME>Last1</LAST_NAME>
                <ALT_NAMES></ALT_NAMES>
                <TYPES>
                    <TYPE><CODE>PRI</CODE></TYPE>
                    <TYPE><CODE>PRF</CODE></TYPE>
                </TYPES>
            </NAME>
        </NAMES>
        <GROUPS></GROUPS>
    </PERSON>
</MSG>'''

    static final String MULTI_NAME_XML = '''<MSG>
    <PERSON>
        <ID>1</ID>
        <NAMES>
            <NAME>
                <FIRST_NAME>First1</FIRST_NAME>
                <LAST_NAME>Last1</LAST_NAME>
                <ALT_NAMES></ALT_NAMES>
                <TYPES>
                    <TYPE><CODE>PRI</CODE></TYPE>
                </TYPES>
            </NAME>
            <NAME>
                <FIRST_NAME>First2</FIRST_NAME>
                <LAST_NAME>Last2</LAST_NAME>
                <ALT_NAMES></ALT_NAMES>
                <TYPES>
                    <TYPE><CODE>PRI</CODE></TYPE>
                    <TYPE><CODE>PRF</CODE></TYPE>
                </TYPES>
            </NAME>
        </NAMES>
        <GROUPS></GROUPS>
    </PERSON>
</MSG>'''
}
