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
package edu.berkeley.bidms.app;

import edu.berkeley.bidms.app.springsecurity.encoder.DigestAuthPasswordEncoder;
import edu.berkeley.bidms.app.springsecurity.service.RegistryUserCredentialService;
import edu.berkeley.bidms.springsecurity.util.ExtendedDelegatingPasswordEncoder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;

import java.util.Map;

@EnableJms
@SpringBootApplication(scanBasePackages = "edu.berkeley.bidms.app")
public class BidmsApplication {
    private static final BCryptPasswordEncoder.BCryptVersion BCRYPT_VERSION = BCryptPasswordEncoder.BCryptVersion.$2B;
    private static final int BCRYPT_STRENGTH = 12;

    public static void main(String[] args) {
        SpringApplication.run(BidmsApplication.class, args);
    }

    @Bean(name = RegistryUserCredentialService.DIGEST_AUTH_PASSWORD_ENCODER_BEAN_NAME)
    public DigestAuthPasswordEncoder getDigestAuthPasswordEncoder() {
        return new DigestAuthPasswordEncoder("Registry Realm");
    }

    @Bean(name = RegistryUserCredentialService.PASSWORD_ENCODER_BEAN_NAME)
    public DelegatingPasswordEncoder getPasswordEncoder(DigestAuthPasswordEncoder digestAuthPasswordEncoder) {
        // The decode default is 'digest' because the bcrypt encoder
        // generates encoded passwords with the prefix built-into them.
        // Therefore, we assume that non-prefixed passwords must be digest
        // passwords.  The use of digest auth is utilized only sparingly
        // to support orgs that still have a few legacy endpoints.
        return new ExtendedDelegatingPasswordEncoder(
                "bcrypt",
                "digest",
                Map.of(
                        "bcrypt", new BCryptPasswordEncoder(BCRYPT_VERSION, BCRYPT_STRENGTH),
                        "digest", digestAuthPasswordEncoder
                )
        );
    }
}
