/*
 * Copyright (c) 2015, Regents of the University of California and
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
package edu.berkeley.bidms.app.matchservice.service

import edu.berkeley.bidms.app.matchservice.PersonExactMatch
import edu.berkeley.bidms.app.matchservice.PersonExistingMatch
import edu.berkeley.bidms.app.matchservice.PersonMatch
import edu.berkeley.bidms.app.matchservice.PersonNoMatch
import edu.berkeley.bidms.app.matchservice.PersonPartialMatch
import edu.berkeley.bidms.app.matchservice.PersonPartialMatches
import edu.berkeley.bidms.app.registryModel.model.PartialMatch
import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.model.history.MatchHistory
import edu.berkeley.bidms.app.registryModel.model.history.MatchHistoryMetaData
import edu.berkeley.bidms.app.registryModel.model.type.MatchHistoryResultTypeEnum
import edu.berkeley.bidms.app.registryModel.repo.PartialMatchRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.history.MatchHistoryRepository
import groovy.util.logging.Slf4j
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service("matchServiceDatabaseService")
@Transactional(rollbackFor = Exception)
class DatabaseService {

    EntityManager entityManager
    SORObjectRepository sorObjectRepository
    PartialMatchRepository partialMatchRepository
    MatchHistoryRepository matchHistoryRepository

    DatabaseService(
            EntityManager entityManager,
            SORObjectRepository sorObjectRepository,
            PartialMatchRepository partialMatchRepository,
            MatchHistoryRepository matchHistoryRepository
    ) {
        this.entityManager = entityManager
        this.sorObjectRepository = sorObjectRepository
        this.partialMatchRepository = partialMatchRepository
        this.matchHistoryRepository = matchHistoryRepository
    }

    /**
     * Assign a person to a SORObject, linking the two together.
     * @param sorObject
     * @param person
     */
    void assignUidToSOR(SORObject sorObject, Person person) {
        sorObject.person = person
        // clear sorObject out of PartialMatch table if it's there
        removeExistingPartialMatches(sorObject)
        sorObjectRepository.saveAndFlush(sorObject)
    }

    /**
     * Store a potential match(es), linking a sorObject to the People in the Database
     */
    void storePartialMatch(SORObject sorObject, List<PersonPartialMatch> matchingPeople) {
        removeExistingPartialMatches(sorObject)
        matchingPeople.each {
            createPartialMatch(sorObject, it)
        }
    }

    private void createPartialMatch(SORObject sorObject, PersonPartialMatch personPartialMatch) {
        PartialMatch partialMatch = partialMatchRepository.findBySorObjectAndPerson(sorObject, personPartialMatch.person)
        if (!partialMatch) {
            partialMatch = new PartialMatch(personPartialMatch.person)
            partialMatch.sorObject = sorObject
        }
        try {
            partialMatch.metaData.eventId = personPartialMatch.eventId
            partialMatch.metaData.ruleNames = personPartialMatch.ruleNames
            partialMatchRepository.saveAndFlush(partialMatch)
        } catch (e) {
            log.error("Failed to save PartialMatch for SORObject: ${sorObject}, Person: ${personPartialMatch}", e)
        }
    }

    void removeExistingPartialMatches(SORObject sorObject) {
        List<PartialMatch> partialMatches = partialMatchRepository.findAllBySorObject(sorObject)
        partialMatches.each {
            partialMatchRepository.delete(it)
        }
        entityManager.flush()
    }

    MatchHistory recordMatchHistory(SORObject sorObject, PersonMatch personMatch, String newlyGeneratedUid) {
        if (personMatch instanceof PersonExistingMatch) {
            // if PersonExistingMatch, then sorObject was already assigned to a uid and no action was taken
            return null
        }
        Date actionTime = new Date()
        MatchHistory matchHistory = new MatchHistory(
                eventId: personMatch.eventId,
                sorObjectId: sorObject.id,
                sorId: sorObject.sor.id,
                sorPrimaryKey: sorObject.sorPrimaryKey,
                actionTime: actionTime
        )

        if (personMatch instanceof PersonExactMatch) {
            matchHistory.matchResultType = MatchHistoryResultTypeEnum.EXACT
            matchHistory.uidAssigned = ((PersonExactMatch) personMatch).person.uid
            matchHistory.metaData.exactMatch = new MatchHistoryMetaData.MatchHistoryExactMatch(
                    ruleNames: ((PersonExactMatch) personMatch).ruleNames
            )
        } else if (personMatch instanceof PersonPartialMatches) {
            matchHistory.matchResultType = MatchHistoryResultTypeEnum.POTENTIAL
            int fullPotentialMatchCount = ((PersonPartialMatches) personMatch).partialMatches.size()
            matchHistory.metaData.potentialMatchCount = fullPotentialMatchCount
            // Don't store more than 64, which is already an excessive amount.  More than this means a bad match rule that should be fixed.
            int potentialMatchCount = fullPotentialMatchCount > 64 ? 64 : fullPotentialMatchCount
            matchHistory.metaData.potentialMatches = new ArrayList<>(potentialMatchCount)
            for (int i = 0; i < potentialMatchCount; i++) {
                PersonPartialMatch partialMatch = ((PersonPartialMatches) personMatch).partialMatches[i]
                matchHistory.metaData.potentialMatches << new MatchHistoryMetaData.MatchHistoryPartialMatch(
                        potentialMatchToUid: partialMatch.person.uid,
                        ruleNames: partialMatch.ruleNames
                )
            }
        } else if (personMatch instanceof PersonNoMatch) {
            if (((PersonNoMatch) personMatch).matchOnly) {
                matchHistory.matchResultType = MatchHistoryResultTypeEnum.NONE_MATCH_ONLY
            } else if (newlyGeneratedUid) {
                matchHistory.matchResultType = MatchHistoryResultTypeEnum.NONE_NEW_UID
                matchHistory.uidAssigned = newlyGeneratedUid
            } else {
                // uid is to be assigned but it's deferred for later assignment
                matchHistory.matchResultType = MatchHistoryResultTypeEnum.NONE_NEW_UID_DEFERRED
            }
        }

        return matchHistoryRepository.saveAndFlush(matchHistory)
    }
}
