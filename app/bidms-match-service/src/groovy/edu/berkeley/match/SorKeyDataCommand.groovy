package edu.berkeley.match

import edu.berkeley.registry.model.SORObject
import grails.validation.Validateable
import groovy.util.logging.Log4j

@Validateable
@Log4j
class SorKeyDataCommand {
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

    SORObject getSorObject() {
        log.debug("Loading SORObject for $systemOfRecord/$sorPrimaryKey")
        def sorObject = SORObject.getBySorAndObjectKey(systemOfRecord, sorPrimaryKey)
        log.debug("-- found: $sorObject")
        return sorObject
    }

    Map getAttributes() {
        def sorAttributes = NewSORConsumerService.MATCH_FIELDS.findAll { this[it] }.collectEntries { [it, this[it].toString()] }
        if(otherIds) {
            sorAttributes.otherIds = otherIds
        }
        sorAttributes
    }


    static constraints = {
        systemOfRecord nullable: false
        sorPrimaryKey nullable: false, validator: { value, object ->
            if(!object.sorObject) {
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
    }

}
