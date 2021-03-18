/*
 * Copyright (c) 2020, Regents of the University of California and
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
package edu.berkeley.bidms.app

import edu.berkeley.bidms.app.springsecurity.encoder.DigestAuthPasswordEncoder
import edu.berkeley.bidms.app.springsecurity.service.RegistryUserCredentialService
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.jms.annotation.EnableJms
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.transaction.annotation.EnableTransactionManagement

@EnableTransactionManagement
@EnableJms
@SpringBootApplication(scanBasePackages = "edu.berkeley.bidms.app")
class TestApplication {
    private static final BCryptPasswordEncoder.BCryptVersion BCRYPT_VERSION = BCryptPasswordEncoder.BCryptVersion.$2B;
    private static final int BCRYPT_STRENGTH = 12;

    static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args)
    }

    @Bean(name = RegistryUserCredentialService.DIGEST_AUTH_PASSWORD_ENCODER_BEAN_NAME)
    DigestAuthPasswordEncoder getDigestAuthPasswordEncoder() {
        return new DigestAuthPasswordEncoder("Registry Realm")
    }

    @Bean(name = RegistryUserCredentialService.PASSWORD_ENCODER_BEAN_NAME)
    DelegatingPasswordEncoder getPasswordEncoder() {
        return new DelegatingPasswordEncoder(
                "bcrypt",
                Map.of("bcrypt", new BCryptPasswordEncoder(BCRYPT_VERSION, BCRYPT_STRENGTH))
        )
    }
}
