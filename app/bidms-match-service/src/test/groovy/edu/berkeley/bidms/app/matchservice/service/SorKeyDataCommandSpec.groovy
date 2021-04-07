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

import edu.berkeley.bidms.app.matchservice.SorKeyDataCommand
import edu.berkeley.bidms.app.registryModel.model.SOR
import edu.berkeley.bidms.app.registryModel.model.SORObject
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification
import spock.lang.Unroll

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class SorKeyDataCommandSpec extends Specification {

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    def setup() {
        sorObjectRepository.saveAndFlush(new SORObject(
                sor: sorRepository.save(new SOR(name: 'SIS')),
                sorPrimaryKey: '12345',
                objJson: '{}',
                jsonVersion: 1,
                queryTime: new Date()
        ))
    }

    @Unroll
    def "test that it's possible to get the SORObject, if sor and primay key are correct"() {
        when:
        def command = new SorKeyDataCommand(
                sorRepository: sorRepository,
                sorObjectRepository: sorObjectRepository,
                systemOfRecord: sor,
                sorPrimaryKey: sorPk
        )

        then:
        command.sorObject?.sor?.name == expectedSor
        command.sorObject?.sorPrimaryKey == expectedSorPk

        where:
        sor    | sorPk   | expectedSor | expectedSorPk
        'SIS'  | '12345' | 'SIS'       | '12345'
        'HRMS' | '12345' | null        | null
        'SIS'  | '23456' | null        | null
        null   | null    | null        | null
    }

    @Unroll
    def "test that get attributes returns the correct values"() {
        when:
        def command = new SorKeyDataCommand(
                sorRepository: sorRepository,
                sorObjectRepository: sorObjectRepository,
                systemOfRecord: 'SIS',
                sorPrimaryKey: '123',
                givenName: givenName,
                middleName: middleName,
                surName: surName,
                dateOfBirth: dateOfBirth,
                otherIds: otherIds,
                matchOnly: matchOnly
        )

        then:
        command.attributes.systemOfRecord == 'SIS'
        command.attributes.sorPrimaryKey == '123'
        command.attributes.keySet().sort() == expectedOutputAttributes.sort()

        where:
        givenName | middleName | surName | dateOfBirth | otherIds       | matchOnly | expectedOutputAttributes
        null      | null       | null    | null        | null           | null      | ['systemOfRecord', 'sorPrimaryKey']
        null      | null       | null    | null        | [:]            | null      | ['systemOfRecord', 'sorPrimaryKey']
        'b'       | null       | null    | 'e'         | [:]            | null      | ['systemOfRecord', 'sorPrimaryKey', 'givenName', 'dateOfBirth']
        'b'       | 'c'        | 'd'     | 'e'         | [:]            | null      | ['systemOfRecord', 'sorPrimaryKey', 'givenName', 'middleName', 'surName', 'dateOfBirth']
        'b'       | null       | null    | 'e'         | [kryf: 'plyf'] | null      | ['systemOfRecord', 'sorPrimaryKey', 'givenName', 'dateOfBirth', 'otherIds']
        null      | null       | null    | null        | null           | true      | ['systemOfRecord', 'sorPrimaryKey', 'matchOnly']
        null      | null       | null    | null        | null           | 'true'    | ['systemOfRecord', 'sorPrimaryKey', 'matchOnly']
    }
}
