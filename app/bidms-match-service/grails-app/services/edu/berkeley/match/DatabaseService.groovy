package edu.berkeley.match

import grails.transaction.Transactional

@Transactional
class DatabaseService {

    void assignUidToSOR(String systemOfRecord, String sorIdentifier, String uid) {

    }

    void storePartialMatch(String systemOfRecord, String sorIdentifier, List<String> matchingUids) {

    }
}
