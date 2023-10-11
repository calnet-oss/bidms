/*
 * Copyright (c) 2023, Regents of the University of California and
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
package edu.berkeley.bidms.app.registryModel.model.history

import edu.berkeley.bidms.app.registryModel.model.type.MatchHistoryResultTypeEnum
import edu.berkeley.bidms.app.registryModel.repo.history.MatchHistoryRepository
import edu.berkeley.bidms.common.json.JsonUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class MatchHistorySpec extends Specification {
    @Autowired
    MatchHistoryRepository matchHistoryRepository

    void "test MatchHistory metaData JSON serialization upon saving"() {
        given: "a Match history entry"
        def actionTime = Date.parse("yyyy-MM-dd", "2023-10-11", TimeZone.getTimeZone("GMT"))
        def matchHistory = new MatchHistory().with {
            eventId = "event123"
            sorObjectId = 123
            sorPrimaryKey = "key"
            sorId = 1
            matchResultType = MatchHistoryResultTypeEnum.EXACT
            it.actionTime = actionTime
            uidAssigned = "uid123"
            metaData.with {
                exactMatch = new MatchHistoryMetaData.MatchHistoryExactMatch(ruleNames: ['TEST_RULE'])
                it
            }
            it
        }

        when: "persisted"
        matchHistory = matchHistoryRepository.save(matchHistory)

        and:
        def matchHistoryJson = JsonUtil.convertObjectToJson(matchHistory)
        println matchHistoryJson

        then: "metaData serialized to JSON"
        matchHistory.id
        matchHistory.metaDataJson == '{"exactMatch":{"ruleNames":["TEST_RULE"]}}'

        and: "the whole MatchHistory object serialized to JSON"
        matchHistoryJson == """{"id":${matchHistory.id},"eventId":"event123","sorId":1,"sorObjectId":123,"sorPrimaryKey":"key","matchResultType":"EXACT","actionTime":"2023-10-11T00:00:00Z","uidAssigned":"uid123","metaData":{"exactMatch":{"ruleNames":["TEST_RULE"]}}}"""

        cleanup:
        matchHistoryRepository.delete(matchHistory)
    }

    void "test MatchHistory metaData JSON serialization upon saving when metaData is empty"() {
        given: "a Match history entry"
        def matchHistory = new MatchHistory().with {
            eventId = "event123"
            sorObjectId = 123
            sorPrimaryKey = "key"
            sorId = 1
            matchResultType = MatchHistoryResultTypeEnum.EXACT
            actionTime = new Date()
            it
        }

        when: "persisted"
        matchHistory = matchHistoryRepository.save(matchHistory)

        then: "metaData serialized to JSON"
        matchHistory.id
        matchHistory.metaDataJson == '{}'

        cleanup:
        matchHistoryRepository.delete(matchHistory)
    }
}
