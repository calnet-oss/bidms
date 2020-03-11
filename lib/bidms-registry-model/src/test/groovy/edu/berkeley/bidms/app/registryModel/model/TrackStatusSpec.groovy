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
package edu.berkeley.bidms.app.registryModel.model

import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import edu.berkeley.bidms.app.registryModel.repo.TrackStatusRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification
import spock.lang.Unroll

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class TrackStatusSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    TrackStatusRepository trackStatusRepository

    Class<?> getDomainClass() { return TrackStatus }

    void "confirm TrackStatus is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static TrackStatus[] getTestTrackStatuses(PersonRepository personRepository) {
        return [
                new TrackStatus(
                        person: TestUtil.findPerson(personRepository, "1"),
                        trackStatusType: "DELEGATE_TOKEN_EMAIL",
                        description: 'sent'
                ),
                new TrackStatus(
                        person: TestUtil.findPerson(personRepository, "2"),
                        trackStatusType: "DELEGATE_TOKEN_EMAIL",
                        description: 'sent'
                ),
        ]
    }

    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
    }

    static synchronized void insertTrackStatuses(PersonRepository personRepository, TrackStatusRepository trackStatusRepository) {
        getTestTrackStatuses(personRepository).each {
            trackStatusRepository.saveAndFlush(it)
        }
    }

    void "save test"() {
        when:
        insertTrackStatuses(personRepository, trackStatusRepository)
        List<TrackStatus> expected = getTestTrackStatuses(personRepository)
        List<TrackStatus> actual = trackStatusRepository.findAll() as List<TrackStatus>

        then:
        expected == actual
    }

    @Unroll
    def "test metaData serialization"() {
        when: "Set new metaData"
        def sut = new TrackStatus(metaData: metaData)

        and: "trigger beforeSave"
        sut.beforeSave()

        then:
        sut.metaDataJson == expectedString

        where:
        metaData          | expectedString
        null              | '{}'
        [:]               | '{}'
        [x: 'AA']         | '{"x":"AA"}'
        [x: ['AA', 'BB']] | '{"x":["AA","BB"]}'
        [x: [1, 2, 3]]    | '{"x":[1,2,3]}'
    }

    @Unroll
    def "test metaData deserialization"() {
        when:
        def sut = new TrackStatus(metaDataJson: metaDataJson)

        and: "trigger afterLoad"
        sut.afterLoad()

        then:
        sut.metaData == expectedMetaData

        where:
        metaDataJson        | expectedMetaData
        null                | [:]
        ''                  | [:]
        '{}'                | [:]
        '{"x":"AA"}'        | [x: 'AA']
        '{"x":["AA","BB"]}' | [x: ['AA', 'BB']]
        '{"x":[1,2,3]}'     | [x: [1, 2, 3]]
    }
}
