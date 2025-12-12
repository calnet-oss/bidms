/*
 * Copyright (c) 2016, Regents of the University of California and
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

import edu.berkeley.bidms.app.registryModel.model.type.DownstreamObjectOwnershipLevelEnum
import edu.berkeley.bidms.app.registryModel.model.type.DownstreamSystemEnum
import edu.berkeley.bidms.app.registryModel.repo.DownstreamObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.DownstreamSystemRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import edu.berkeley.bidms.common.json.JsonUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class DownstreamObjectSpec extends Specification {

    @Autowired
    SORRepository sorRepository

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    DownstreamSystemRepository downstreamSystemRepository

    @Autowired
    DownstreamObjectRepository downstreamObjectRepository

    Class<?> getDomainClass() { return DownstreamObject }

    void "confirm DownstreamObject is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    def "test that a DownstreamObject can be found when exists"() {
        when:
        DownstreamSystem testDownstreamSystem = downstreamSystemRepository.save(new DownstreamSystem(name: DownstreamSystemEnum.LDAP.name()))
        Person testPerson = personRepository.save(new Person(uid: "person1"))

        and:
        def obj = downstreamObjectRepository.saveAndFlush(new DownstreamObject(
                person: testPerson,
                system: testDownstreamSystem,
                systemPrimaryKey: '123',
                objJson: '{}',
                ownershipLevel: DownstreamObjectOwnershipLevelEnum.OWNED.value
        ))

        then:
        obj.id

        and:
        downstreamObjectRepository.findBySystemAndSystemPrimaryKey(downstreamSystemRepository.findByName(DownstreamSystemEnum.LDAP.name()), '123')
    }

    def "test that finding a DownstreamObject with unknown key returns null"() {
        when:
        DownstreamSystem testDownstreamSystem = downstreamSystemRepository.save(new DownstreamSystem(name: DownstreamSystemEnum.LDAP.name()))
        Person testPerson = personRepository.save(new Person(uid: "person1"))

        and:
        def obj = downstreamObjectRepository.saveAndFlush(new DownstreamObject(
                person: testPerson,
                system: testDownstreamSystem,
                systemPrimaryKey: '123',
                objJson: '{}',
                ownershipLevel: DownstreamObjectOwnershipLevelEnum.OWNED.value
        ))

        then:
        obj.id

        and:
        !downstreamObjectRepository.findBySystemAndSystemPrimaryKey(testDownstreamSystem, 'bogus')
    }

    def "test parsing json"() {
        when:
        DownstreamSystem testDownstreamSystem = downstreamSystemRepository.save(new DownstreamSystem(name: DownstreamSystemEnum.LDAP.name()))
        Person testPerson = personRepository.save(new Person(uid: "person1"))

        and:
        def json = JsonUtil.convertMapToJson([name: 'archer', middleName: null])
        def obj = downstreamObjectRepository.saveAndFlush(new DownstreamObject(
                person: testPerson,
                system: testDownstreamSystem,
                systemPrimaryKey: '123',
                objJson: json,
                ownershipLevel: DownstreamObjectOwnershipLevelEnum.OWNED.value
        ))

        then:
        obj.json.name == 'archer'
        obj.json.containsKey("middleName")
    }

    static DownstreamObject[] getTestDownstreamObjects(PersonRepository personRepository, DownstreamSystemRepository downstreamSystemRepository) {
        Person person = personRepository.get("1")
        return [
                [systemPrimaryKey: "uid1", system: "LDAP"],
                [systemPrimaryKey: "uid2", system: "LDAP"]
        ].collect {
            new DownstreamObject(
                    systemPrimaryKey: it.systemPrimaryKey,
                    system: downstreamSystemRepository.findByName(it.system),
                    person: person,
                    objJson: "{}",
                    ownershipLevel: DownstreamObjectOwnershipLevelEnum.OWNED.value
            )
        }
    }

    static synchronized void insertDownstreamObjects(PersonRepository personRepository, DownstreamSystemRepository downstreamSystemRepository, DownstreamObjectRepository downstreamObjectRepository) {
        getTestDownstreamObjects(personRepository, downstreamSystemRepository).each { downstreamObject ->
            downstreamObjectRepository.saveAndFlush(downstreamObject)
        }
    }

    void "save test"() {
        when:
        DownstreamSystemSpec.insertSystemNames(downstreamSystemRepository)
        PersonSpec.insertPeople(personRepository)
        insertDownstreamObjects(personRepository, downstreamSystemRepository, downstreamObjectRepository)

        then:
        downstreamObjectRepository.findAll().size() > 0
        getTestDownstreamObjects(personRepository, downstreamSystemRepository).each { downstreamObject ->
            downstreamObjectRepository.findBySystemAndSystemPrimaryKey(downstreamObject.system, downstreamObject.systemPrimaryKey).each {
                assert it.systemPrimaryKey == downstreamObject.systemPrimaryKey
                assert it.system.id == downstreamObject.system.id
            }
        }
    }
}
