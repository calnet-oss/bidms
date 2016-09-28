package edu.berkeley.calnet.ucbmatch.database

import groovy.transform.Canonical

@Canonical
class Record {
    List<String> ruleNames
    String referenceId
    boolean exactMatch

}
