package edu.berkeley.registry.model

class PartialMatch {
    SORObject sorObject
    Person person
    static constraints = {
        sorObject nullable: false
        person nullable: false
    }

    static mapping = {
        table name: "PartialMatch"
        id sqlType: "BIGINT"
        version false
        columns {
            sorObject {
                column name: "sorId", sqlType: "SMALLINT"
                column name: "sorObjKey", sqlType: "VARCHAR(64)"
            }
        }
        person column: "personUid", sqlType: "VARCHAR(64)"
    }
}
