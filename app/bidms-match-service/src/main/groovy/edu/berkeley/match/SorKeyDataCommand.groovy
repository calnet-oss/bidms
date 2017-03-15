package edu.berkeley.match

import edu.berkeley.registry.model.SORObject
import grails.validation.Validateable
import groovy.util.logging.Slf4j

@Slf4j
class SorKeyDataCommand implements Validateable {
    // these correspond to properties in SorKeyData from the
    // registry-sor-key-data plugin
    String systemOfRecord
    String sorPrimaryKey
    String uid
    String givenName
    String middleName
    String surName
    String fullName
    String email
    String dateOfBirth
    String socialSecurityNumber
    Map otherIds = [:]
    Boolean matchOnly

    SORObject getSorObject() {
        log.debug("Loading SORObject for $systemOfRecord/$sorPrimaryKey")
        def sorObject = SORObject.getBySorAndObjectKey(systemOfRecord, sorPrimaryKey)
        log.debug("-- found: $sorObject")
        return sorObject
    }

    Map<String, Object> getAttributes() {
        def sorAttributes = NewSORConsumerService.MATCH_STRING_FIELDS.findAll { this[it] }.collectEntries { [it, this[it].toString()] } +
                NewSORConsumerService.MATCH_BOOLEAN_FIELDS.findAll { this[it] }.collectEntries { [it, this[it] as Boolean] }
        if (otherIds) {
            sorAttributes.otherIds = otherIds
        }
        sorAttributes
    }


    static constraints = {
        systemOfRecord nullable: false
        sorPrimaryKey nullable: false, validator: { value, object ->
            if (!object.sorObject) {
                "does.not.match.sorObject"
            }
        }
        uid nullable: true
        givenName nullable: true
        middleName nullable: true
        surName nullable: true
        fullName nullable: true
        email nullable: true
        dateOfBirth nullable: true
        socialSecurityNumber nullable: true
        matchOnly nullable: true
    }
}
