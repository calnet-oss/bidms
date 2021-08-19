/*
 * Copyright (c) 2014, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.bidms.app.matchengine.service

import edu.berkeley.bidms.app.matchengine.ConfidenceType
import edu.berkeley.bidms.app.matchengine.database.Candidate
import edu.berkeley.bidms.app.matchengine.database.Record
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional
class MatchService {

    DatabaseService databaseService

    MatchService(DatabaseService databaseService) {
        this.databaseService = databaseService
    }

    /**
     * Will search the database with for attributes in matchInput combined
     * with the matchConfig.
     */
    Set<Candidate> findCandidates(Map matchInput) {
        Set<Candidate> candidates = null
        log.debug("findCandidates (SuperCanonical) for ${matchInput.systemOfRecord}/${matchInput.identifier}")
        candidates = databaseService.searchDatabase(matchInput, ConfidenceType.SUPERCANONICAL)
        // Super canonical means if anything found, stop searching, as long
        // as there was exactly one exactMatch candidate.  superCanonical
        // rules in the match configuration should be intentionally designed
        // to only ever exactMatch one uid.  superCanonical rules are meant
        // to match with definitive identifiers, not match on demographic
        // data.
        //
        // Example: Data from a database SOR and data from a realtime
        // messaging SOR where it's really the same source system.  The
        // primary key is guaranteed to be the same in both.  If there's
        // more than one exactMatch "super candidate", log a prominent
        // warning.  This should be considered a match configuration
        // problem, or a problem with incoming data.
        //
        // It is possible for a potential super candidate in the results.
        // This happens when there is a row in the PartialMatch table for
        // the other SOR.  Since the other SORObject with a matching
        // identifier is sitting in the PartialMatch table, in this
        // scenario, this SORObject should go there too.
        if ((candidates*.referenceId).unique().size() == 1 && candidates.every { it.exactMatch }) {
            // Matched with exactly one super candidate, stop processing any
            // further rules.
            log.debug("findCandidates found one super candidate for ${matchInput.systemOfRecord}/${matchInput.identifier}")
            return candidates
        } else if (candidates.size() > 1 && candidates.any { it.exactMatch }) {
            // Match configuration rules should be designed to prevent
            // multiple super candidates when there is an exact match, so
            // log this as a major warning.
            log.warn("More than one super candidate found (${candidates.size()}) for ${matchInput.systemOfRecord}/${matchInput.identifier}.  This should not happen.  Check match configuration.  Super candidate referenceIds: ${candidates*.referenceId}")
            // Drop down to rest of rules since there are errant multiple
            // super candidates.
        } else if (candidates.any { !it.exactMatch } && !candidates.any { it.exactMatch }) {
            // There's a SORObject from the other SOR in the PartialMatch
            // table so this SORObject should go into PartialMatch too.
            log.debug("findCandidates found a super candidate that's in the partial match queue so ${matchInput.systemOfRecord}/${matchInput.identifier} is also a potential")
            return candidates
        } else if (candidates.size() > 1) {
            // Shouldn't get here.
            log.warn("findCandidate found more than one super candidate with unexpected states.")
            // Drop down to rest of rules since there are errant multiple
            // super candidates.
        }
        log.debug("findCandidates (Canonical) for ${matchInput.systemOfRecord}/${matchInput.identifier}")
        candidates = databaseService.searchDatabase(matchInput, ConfidenceType.CANONICAL)
        if (!candidates) {
            log.debug("findCandidates (Potential) for ${matchInput.systemOfRecord}/${matchInput.identifier}")
            candidates = databaseService.searchDatabase(matchInput, ConfidenceType.POTENTIAL)
        }
        log.debug("findCandidates found ${candidates.size()} candidates")
        return candidates
    }

    Record findExistingRecord(Map matchInput) {
        return databaseService.findRecord(matchInput.systemOfRecord, matchInput.identifier as String)
    }
}
