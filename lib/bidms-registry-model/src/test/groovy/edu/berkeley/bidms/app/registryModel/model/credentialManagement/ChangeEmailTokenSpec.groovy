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
package edu.berkeley.bidms.app.registryModel.model.credentialManagement

import edu.berkeley.bidms.app.registryModel.model.Person
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository
import edu.berkeley.bidms.app.registryModel.repo.credentialManagement.ChangeEmailTokenRepository
import jakarta.validation.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class ChangeEmailTokenSpec extends Specification {

    @Autowired
    PersonRepository personRepository

    @Autowired
    ChangeEmailTokenRepository changeEmailTokenRepository

    Class<?> getDomainClass() { return ChangeEmailToken }

    void "confirm ChangeEmailToken is comparable"() {
        expect:
        Comparable.isAssignableFrom(getDomainClass())
    }

    def "test that object validates email address and assigns token"() {
        given:
        def person = personRepository.save(new Person(uid: '123'))

        when:
        def changeEmailToken = changeEmailTokenRepository.saveAndFlush(new ChangeEmailToken(person: person, emailAddress: "valid@email.com", expiryDate: new Date() + 1))

        then:
        changeEmailToken.emailAddress
        changeEmailToken.token
    }

    def "test bad email address does not validate"() {
        given:
        def person = personRepository.save(new Person(uid: '123'))

        when:
        changeEmailTokenRepository.saveAndFlush(new ChangeEmailToken(person: person, emailAddress: "invalidemailaddress", expiryDate: new Date() + 1))

        then:
        thrown ConstraintViolationException
    }
}
