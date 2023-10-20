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
package edu.berkeley.bidms.common.json

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import edu.berkeley.bidms.common.json.mod.AddSerializationPropertyModification
import edu.berkeley.bidms.common.json.mod.jackson.ObjectSerializerPropertiesModifier
import spock.lang.Specification

class SerializationModificationSpec extends Specification {

    static final Person person = new Person(
            telephone: new Telephone(id: 1, telephone: "555-5555"),
            email: new Email(id: 1, emailAddress: "test@test.com")
    )

    void "test default serialization behavior"() {
        given: "a mapper"
        def objectMapper = JsonMapper.builder().build()

        when: "person is serialized"
        def map = objectMapper.convertValue(person, Map)

        then: "email does not contain id"
        map.email.emailAddress == "test@test.com"
        !map.email.id

        and: "expected other data is present"
        map.email.emailAddress == "test@test.com"
        map.telephone.telephone == "555-5555"
        !map.telephone.id
    }

    void "test modification of serialization behavior"() {
        given: "a mapper with a serialization modification"
        def objectMapper = JsonMapper.builder()
                .addModule(new SimpleModule().setSerializerModifier(new ObjectSerializerPropertiesModifier(
                        new AddSerializationPropertyModification("id", Email)
                )))
                .build()

        when: "person is serialized"
        def map = objectMapper.convertValue(person, Map)

        then: "email does contain id"
        map.email.id == 1

        and: "expected other data is present"
        map.email.emailAddress == "test@test.com"
        map.telephone.telephone == "555-5555"
        !map.telephone.id
    }

    static class Telephone {
        // id not included in the serialization
        @JsonIgnore
        long id

        String telephone
    }

    static class Email {
        // by default, id is not included in the serialization
        @JsonIgnore
        long id

        String emailType

        String emailAddress
    }

    static class Person {
        String uid

        Telephone telephone

        Email email
    }
}
