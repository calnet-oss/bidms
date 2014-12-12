package edu.berkeley.calnet.ucbmatch.database

import edu.berkeley.calnet.ucbmatch.Identifier

class Candidate extends Record {
    int confidence
    String systemOfRecord
    String dateOfBirth
    Set<Name> names = []
    Set<Identifier> identifiers = []
}
