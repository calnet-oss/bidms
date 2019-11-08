/*
 * Copyright (c) 2019, Regents of the University of California and
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


import edu.berkeley.bidms.app.registryModel.repo.EmailRepository
import edu.berkeley.bidms.app.registryModel.repo.EmailTypeRepository
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository
import edu.berkeley.bidms.app.registryModel.repo.SORRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class EmailSpec extends Specification {

    @Autowired
    SORRepository sorRepository

    @Autowired
    PersonRepository personRepository

    @Autowired
    SORObjectRepository sorObjectRepository

    @Autowired
    EmailTypeRepository emailTypeRepository

    @Autowired
    EmailRepository emailRepository

    Class<?> getDomainClass() { return Email }

    void "confirm Email is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    static Email[] getTestEmails(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, EmailTypeRepository emailTypeRepository) {
        EmailType emailType = emailTypeRepository.findByEmailTypeName("testName")
        return [
                new Email(
                        person: TestUtil.findPerson(personRepository, "1"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "LDAP_PEOPLE", "uid123"),
                        emailType: emailType,
                        emailAddress: 'sarcher@BERKELEY.EDU' // keep upper-case to test emailAddressLowerCase derivation
                ),
                new Email(
                        person: TestUtil.findPerson(personRepository, "2"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "LDAP_GUEST", "uid123"),
                        emailType: emailType,
                        emailAddress: 'sarcher@berkeley.edu'
                ),
                new Email(
                        person: TestUtil.findPerson(personRepository, "3"),
                        sorObject: TestUtil.findSORObject(sorRepository, sorObjectRepository, "LDAP_ADVCON", "uid123"),
                        emailType: emailType,
                        emailAddress: 'sarcher@berkeley.edu'
                )
        ]
    }

    def setup() {
        // make sure dependency objects are added
        PersonSpec.insertPeople(personRepository)
        SORSpec.insertSorNames(sorRepository)
        SORObjectSpec.insertSorObjects(personRepository, sorRepository, sorObjectRepository)
    }

    static synchronized void insertEmails(PersonRepository personRepository, SORRepository sorRepository, SORObjectRepository sorObjectRepository, EmailTypeRepository emailTypeRepository, EmailRepository emailRepository) {
        emailTypeRepository.save(new EmailType(emailTypeName: "testName"))
        // assign right uid to the SORObjects
        ["LDAP_PEOPLE", "LDAP_GUEST", "LDAP_ADVCON"].eachWithIndex { String entry, int i ->
            SORObject sorObject = TestUtil.findSORObject(sorRepository, sorObjectRepository, entry, "uid123")
            sorObject.person = TestUtil.findPerson(personRepository, (i + 1).toString())
            sorObjectRepository.save(sorObject)
        }
        getTestEmails(personRepository, sorRepository, sorObjectRepository, emailTypeRepository).each {
            emailRepository.saveAndFlush(it)
        }
    }

    void "save test"() {
        when:
        insertEmails(personRepository, sorRepository, sorObjectRepository, emailTypeRepository, emailRepository)
        List<Email> expected = getTestEmails(personRepository, sorRepository, sorObjectRepository, emailTypeRepository)
        List<Email> actual = emailRepository.findAll() as List<Email>

        then:
        expected == actual
        actual.each {
            assert it.emailAddress.toLowerCase() == "sarcher@berkeley.edu"
            assert it.emailAddress.toLowerCase() == it.emailAddressLowerCase
        }
    }

    void "query for email address using emailAddressLowerCase"() {
        when:
        insertEmails(personRepository, sorRepository, sorObjectRepository, emailTypeRepository, emailRepository)
        SORObject sorObject = TestUtil.findSORObject(sorRepository, sorObjectRepository,
                "LDAP_PEOPLE",
                "uid123"
        )
        assert sorObject
        Email email = emailRepository.findBySorObjectAndEmailAddressLowerCase(sorObject, "sarcher@berkeley.edu")

        then:
        email
        email.emailAddress == "sarcher@BERKELEY.EDU"
        email.emailAddressLowerCase == email.emailAddress.toLowerCase()
    }
}
