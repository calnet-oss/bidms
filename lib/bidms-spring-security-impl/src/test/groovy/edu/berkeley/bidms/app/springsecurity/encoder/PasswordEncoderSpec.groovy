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
package edu.berkeley.bidms.app.springsecurity.encoder

import edu.berkeley.bidms.springsecurity.util.ExtendedDelegatingPasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class PasswordEncoderSpec extends Specification {
    @Shared
    DigestAuthPasswordEncoder httpDigestPasswordEncoder

    @Shared
    DelegatingPasswordEncoder delegatingPasswordEncoder

    static final def BCRYPT_VERSION = BCryptPasswordEncoder.BCryptVersion.$2B
    static final def BCRYPT_STRENGTH = 12

    void setupSpec() {
        httpDigestPasswordEncoder = new DigestAuthPasswordEncoder("Registry Realm")
        delegatingPasswordEncoder = new ExtendedDelegatingPasswordEncoder(
                "bcrypt",
                "digest",
                [
                        "bcrypt": new BCryptPasswordEncoder(BCRYPT_VERSION, BCRYPT_STRENGTH),
                        "digest": httpDigestPasswordEncoder
                ]
        )
    }

    void "test delegating bcrypt password encoder"() {
        when:
        def encoded = delegatingPasswordEncoder.encode("foopassword")

        then:
        encoded.startsWith('{bcrypt}$2b$12$')
        delegatingPasswordEncoder.matches("foopassword", encoded)
    }

    void "test httpDigestPasswordEncoder"() {
        expect:
        httpDigestPasswordEncoder.encode("{foouser}foopassword") == "eb1d53d8b371d14c5e0d563f01393320"
    }

    @Unroll
    void "test matching passwords with delegating password encoder for #type"() {
        when:
        boolean matches = delegatingPasswordEncoder.matches(rawPassword, encodedPassword)

        then:
        matches

        where:
        type     | rawPassword                        | encodedPassword
        'bcrypt' | 'foopassword'                      | '{bcrypt}$2b$12$z.TmHoSo/lXpdfzBlafsZuOOHAAfZallYGk1Gb3cQZHiMExo8tvHi'
        'digest' | 'eb1d53d8b371d14c5e0d563f01393320' | '{digest}eb1d53d8b371d14c5e0d563f01393320'
        'digest' | 'eb1d53d8b371d14c5e0d563f01393320' | 'eb1d53d8b371d14c5e0d563f01393320'
    }
}
