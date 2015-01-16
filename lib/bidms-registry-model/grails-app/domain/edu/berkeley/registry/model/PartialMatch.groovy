package edu.berkeley.registry.model

class PartialMatch {
    SORObject sorObject
    Person person
    static constraints = {
        sorObject nullable: false
        person nullable: false
    }

    static mapping = {

    }
}
