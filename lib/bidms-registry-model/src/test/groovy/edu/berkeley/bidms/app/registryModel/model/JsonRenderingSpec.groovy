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


import edu.berkeley.bidms.app.registryModel.repo.DateOfBirthRepository
import edu.berkeley.bidms.app.registryModel.repo.IdentifierArchiveRepository
import edu.berkeley.bidms.app.registryModel.repo.IdentifierRepository
import edu.berkeley.bidms.app.registryModel.repo.IdentifierTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.NameTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonNameRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import edu.berkeley.bidms.common.json.JsonUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

import javax.persistence.EntityManager

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class JsonRenderingSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    NameTypeRepository nameTypeRepository

    @Autowired
    PersonNameRepository personNameRepository

    @Autowired
    IdentifierTypeRepository identifierTypeRepository

    @Autowired
    IdentifierRepository identifierRepository

    @Autowired
    DateOfBirthRepository dateOfBirthRepository

    @Autowired
    IdentifierArchiveRepository identifierArchiveRepository

    @Autowired
    EntityManager entityManager

    void setup() {
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
        SORObjectSpec.insertSorObjects(personRepository, sorRepository, sorObjectRepository)
        PersonNameSpec.insertNames(personRepository, sorRepository, sorObjectRepository, nameTypeRepository, personNameRepository)
        IdentifierSpec.insertIdentifiers(personRepository, sorRepository, sorObjectRepository, identifierTypeRepository, identifierRepository)
        DateOfBirthSpec.insertDates(personRepository, sorRepository, sorObjectRepository, dateOfBirthRepository)
    }

    // #sorObjectId gets string-replaced
    private static String expectedJSON = '''{
  "datesOfBirth" : [ {
    "dateOfBirth" : "1999-01-03T08:00:00Z",
    "dateOfBirthMMDD" : "0301",
    "sorObjectId" : #sorObjectId
  } ],
  "identifiers" : [ {
    "identifier" : "hr123",
    "identifierType" : {
      "idName" : "hrId"
    },
    "isPrimary" : false,
    "sorObjectId" : #sorObjectId,
    "weight" : 0
  } ],
  "isLocked" : false,
  "names" : [ {
    "givenName" : "John",
    "isPrimary" : false,
    "middleName" : "M",
    "nameType" : {
      "typeName" : "testName"
    },
    "prefix" : "Mr",
    "sorObjectId" : #sorObjectId,
    "surName" : "Smith"
  } ],
  "uid" : "1"
}'''

    void "test person to json"() {
        when:
        entityManager.flush()
        Person person = personRepository.get("1")
        entityManager.refresh(person)
        String json = JsonUtil.convertObjectToJson(person, true, true)

        and: "do sorObjectId string replacement"
        def expectedJSONStringReplaced = expectedJSON.replaceAll("#sorObjectId", person.names.first().sorObjectId.toString())

        then:
        json == expectedJSONStringReplaced
    }
}
