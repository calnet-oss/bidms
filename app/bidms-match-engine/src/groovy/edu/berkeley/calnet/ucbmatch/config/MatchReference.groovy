package edu.berkeley.calnet.ucbmatch.config

import edu.berkeley.calnet.ucbmatch.database.IdGenerator

class MatchReference {
    Class<? extends IdGenerator> idGenerator
    String responseType
}
