package edu.berkeley.registry.model.view
/**
 * This is a read-only view of a person, build on top of NEW_MATCH_VIEW
 */
class PersonSearchView {
    String id
    String personNameType
    String fullName
    String givenName
    String middleName
    String surName
    String dateOfBirthString
    Date dateOfBirth
    String identifierType
    String identifier
    String emailType
    String emailAddress

    static constraints = {
    }

    def beforeInsert() {
        throw new IllegalStateException("Object cannot be inserted")
    }

    def beforeUpdate() {
        throw new IllegalStateException("Object cannot be updated")
    }

    def beforeDelete() {
        throw new IllegalStateException("Object cannot be deleted")
    }

    static mapping = {
        table "PersonSearchView"

        id column: 'uid', sqlType: 'varchar(64)'
        version false

        personNameType column: 'personNameType', sqlType: 'VARCHAR(64)'
        fullName column: 'fullName', sqlType: 'VARCHAR(255)'
        givenName column: 'givenName', sqlType: 'VARCHAR(127)'
        middleName column: 'middleName', sqlType: 'VARCHAR(127)'
        surName column: 'surName', sqlType: 'VARCHAR(127)'
        dateOfBirthString column: 'dateOfBirthText', sqlType: 'TEXT'
        dateOfBirth column: 'dateOfBirth', sqlType: 'DATE'
        identifierType column: 'identifierType', sqlType: 'VARCHAR(64)'
        identifier column: 'identifier', sqlType: 'VARCHAR(64)'
        emailType column: 'emailType', sqlType: 'VARCHAR(64)'
        emailAddress column: 'emailAddress', sqlType: 'VARCHAR(255)'
    }

    public String getUid() {
        return id
    }
}
