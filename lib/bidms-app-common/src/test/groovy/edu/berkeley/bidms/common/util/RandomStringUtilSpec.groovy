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
package edu.berkeley.bidms.common.util

import spock.lang.Specification
import spock.lang.Unroll

import static RandomStringUtil.CharTemplate
import static RandomStringUtil.randomString

class RandomStringUtilSpec extends Specification {

    def "test randomString with no specific requirements on the first chars"() {
        // Repeating to make sure that the pattern is correct
        when: "creating 100 random strings"
        def result = (1..100).collect { randomString(10) }

        then:
        result.every { it ==~ /^[0-9a-zA-Z]{10}$/ }
    }

    @Unroll
    def "test randomString with specific requirements on the first chars #args"() {
        // Repeating to make sure that the pattern is correct
        when: "creating 100 random strings"
        def result = (1..100).collect { randomString(10, *args) }

        then: "all strings matches the regex"
        result.every { it ==~ regex }

        where:
        args                                                               | regex
        [CharTemplate.ALPHA, CharTemplate.ALPHA]                           | /^[a-zA-Z]{2}[0-9a-zA-Z]{8}$/
        [CharTemplate.LOWER_ALPHA, CharTemplate.UPPER_ALPHA]               | /^[a-z][A-Z][0-9a-zA-Z]{8}$/
        [CharTemplate.NUMERIC]                                             | /^[0-9][0-9a-zA-Z]{9}$/
        [CharTemplate.LOWER_ALPHANUMERIC, CharTemplate.UPPER_ALPHANUMERIC] | /^[0-9a-z][0-9A-Z][0-9a-zA-Z]{8}$/
    }
}
