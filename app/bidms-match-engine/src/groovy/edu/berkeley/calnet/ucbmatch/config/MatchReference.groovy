package edu.berkeley.calnet.ucbmatch.config

import edu.berkeley.calnet.ucbmatch.database.IdGenerator

class MatchReference {
    Class<? extends IdGenerator> idGenerator
    String responseType
    String systemOfRecordAttribute // Would normally be 'sor'
    String identifierAttribute  // Would normally be 'sorid'
}
