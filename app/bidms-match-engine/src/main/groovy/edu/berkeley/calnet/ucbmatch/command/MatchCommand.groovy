package edu.berkeley.calnet.ucbmatch.command

import grails.validation.Validateable
import groovy.transform.ToString

@ToString(includeNames = true)
class MatchCommand implements Validateable {
    String systemOfRecord
    String identifier
    Map sorAttributes
}
