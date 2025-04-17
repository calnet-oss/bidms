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


import edu.berkeley.bidms.app.registryModel.repo.DelegateProxyRepository
import edu.berkeley.bidms.app.registryModel.repo.DelegateProxyTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.IdentifierRepository
import edu.berkeley.bidms.app.registryModel.repo.IdentifierTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class DelegateProxySpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    IdentifierTypeRepository identifierTypeRepository

    @Autowired
    IdentifierRepository identifierRepository

    @Autowired
    DelegateProxyTypeRepository delegateProxyTypeRepository

    @Autowired
    DelegateProxyRepository delegateProxyRepository

    Class<?> getDomainClass() { return DelegateProxy }

    void "confirm DelegateProxy is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static DelegateProxy[] getTestDelegateProxies(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, DelegateProxyTypeRepository delegateProxyTypeRepository) {
        return [
                new DelegateProxy(
                        person: TestUtil.findPerson(personRepository, "1"),
                        sourceProxyId: "PROXY123",
                        delegateProxyType: delegateProxyTypeRepository.findByDelegateProxyTypeName("sisDelegateProxy"),
                        delegateProxySorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "SIS_DELEGATE", "sisDelegate123"),
                        proxyForId: "EMPLID123",
                        proxyForDisplayName: "Std Display"
                )
        ]
    }

    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
        SORObjectSpec.insertSorObjects(personRepository, sorRepository, sorObjectRepository)
        IdentifierType identifierType = identifierTypeRepository.saveAndFlush(new IdentifierType(idName: "sisStudentId"))
        identifierRepository.save(new Identifier(person: personRepository.get('1'), identifier: 'EMPLID123', sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "SIS_DELEGATE", 'sisDelegate123'), identifierType: identifierType))

        DelegateProxyType delegateProxyType = delegateProxyTypeRepository.saveAndFlush(new DelegateProxyType(delegateProxyTypeName: "sisDelegateProxy"))
        assert delegateProxyType
    }


    static synchronized void insertDelegateProxies(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, DelegateProxyTypeRepository delegateProxyTypeRepository, DelegateProxyRepository delegateProxyRepository) {
        // assign right uid to the SORObjects
        [["SIS_DELEGATE", "sisDelegate123"]].eachWithIndex { List<String> entry, int i ->
            SORObject sorObject = TestUtil.findSORObject(sorRepository, sorObjectRepository, entry[0], entry[1])
            sorObject.person = TestUtil.findPerson(personRepository, (i + 1).toString())
            sorObjectRepository.save(sorObject)
        }
        getTestDelegateProxies(personRepository, sorRepository, sorObjectRepository, delegateProxyTypeRepository).each {
            delegateProxyRepository.saveAndFlush(it)
        }
    }

    void "save test"() {
        when:
        insertDelegateProxies(personRepository, sorRepository, sorObjectRepository, delegateProxyTypeRepository, delegateProxyRepository)
        List<DelegateProxy> expected = getTestDelegateProxies(personRepository, sorRepository, sorObjectRepository, delegateProxyTypeRepository)
        List<DelegateProxy> actual = delegateProxyRepository.findAll() as List<DelegateProxy>

        then:
        expected == actual
    }

    void "get identifier from delegateProxy"() {
        when:
        insertDelegateProxies(personRepository, sorRepository, sorObjectRepository, delegateProxyTypeRepository, delegateProxyRepository)
        def delegateProxy = delegateProxyRepository.findBySourceProxyId('PROXY123')

        then:
        delegateProxy.getProxyForIdentifier(identifierTypeRepository, identifierRepository, "sisStudentId").identifier == 'EMPLID123'
    }

    void "get person from delegateProxy"() {
        when:
        insertDelegateProxies(personRepository, sorRepository, sorObjectRepository, delegateProxyTypeRepository, delegateProxyRepository)
        def delegateProxy = delegateProxyRepository.findBySourceProxyId('PROXY123')

        then:
        delegateProxy.getProxyForPerson(identifierTypeRepository, identifierRepository, "sisStudentId").uid == '1'
    }
}
