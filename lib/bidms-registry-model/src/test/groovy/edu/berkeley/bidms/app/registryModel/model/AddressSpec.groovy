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


import edu.berkeley.bidms.app.registryModel.repo.AddressRepository
import edu.berkeley.bidms.app.registryModel.repo.AddressTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class AddressSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORRepository sorRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    AddressTypeRepository addressTypeRepository

    @Autowired
    AddressRepository addressRepository

    Class<?> getDomainClass() { return Address }

    void "confirm Address is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static Address[] getTestAddresses(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, AddressTypeRepository addressTypeRepository) {
        AddressType addressType = addressTypeRepository.findByAddressTypeName("testName")
        assert addressType
        return [
                new Address(
                        person: TestUtil.findPerson(personRepository, "1"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "LDAP_PEOPLE", "uid123"),
                        addressType: addressType,
                        address1: '123 Fort Rd',
                        address2: 'Suite 100',
                        city: 'Berkeley',
                        regionState: 'CA',
                        postalCode: '94115-5647',
                        country: 'US'
                ),
                new Address(
                        person: TestUtil.findPerson(personRepository, "2"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "LDAP_GUEST", "uid123"),
                        addressType: addressType,
                        address1: '123 Fort Rd',
                        city: 'Berkeley',
                        regionState: 'CA',
                        postalCode: '94115',
                ),
                new Address(
                        person: TestUtil.findPerson(personRepository, "3"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "LDAP_ADVCON", "uid123"),
                        addressType: addressType,
                        city: 'Berkeley',
                        regionState: 'CA',
                        postalCode: '94115',
                )
        ]
    }

    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
        SORObjectSpec.insertSorObjects(personRepository, sorRepository, sorObjectRepository)
    }

    static synchronized void insertAddresses(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, AddressTypeRepository addressTypeRepository, AddressRepository addressRepository) {
        addressTypeRepository.saveAndFlush(new AddressType(addressTypeName: "testName"))
        // assign right uid to the SORObjects
        ["LDAP_PEOPLE", "LDAP_GUEST", "LDAP_ADVCON"].eachWithIndex { String entry, int i ->
            SORObject sorObject = TestUtil.findSORObject(sorRepository, sorObjectRepository, entry, "uid123")
            sorObject.person = TestUtil.findPerson(personRepository, (i + 1).toString())
            sorObjectRepository.save(sorObject)
        }
        getTestAddresses(personRepository, sorRepository, sorObjectRepository, addressTypeRepository).each {
            addressRepository.saveAndFlush(it)
        }
    }

    void "save test"() {
        when:
        insertAddresses(personRepository, sorRepository, sorObjectRepository, addressTypeRepository, addressRepository)
        List<Address> expected = getTestAddresses(personRepository, sorRepository, sorObjectRepository, addressTypeRepository)
        List<Address> actual = addressRepository.findAll() as List<Address>

        then:
        expected == actual
    }
}
