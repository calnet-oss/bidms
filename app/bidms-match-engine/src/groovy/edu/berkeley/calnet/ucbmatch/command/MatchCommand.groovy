package edu.berkeley.calnet.ucbmatch.command

import grails.validation.Validateable
import groovy.transform.ToString

@ToString(includeNames = true)
@Validateable
class MatchCommand {
    String systemOfRecord
    String identifier
    Map sorAttributes
}
