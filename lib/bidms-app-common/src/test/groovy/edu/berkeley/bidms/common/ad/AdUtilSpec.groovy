/*
 * Copyright (c) 2024, Regents of the University of California and
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
package edu.berkeley.bidms.common.ad

import spock.lang.Specification

import java.text.SimpleDateFormat
import java.time.Instant

class AdUtilSpec extends Specification {

    void "test convertObjectGUIDToUUID"() {
        when:
        def result = AdUtil.convertObjectGUIDToUUID(Base64.decoder.decode("kwAtfqTq+0eezGTk5n42xA=="))

        then:
        result.toString() == "7e2d0093-eaa4-47fb-9ecc-64e4e67e36c4"
    }

    void "test ad1601EpochMilliseconds"() {
        given:
        SimpleDateFormat msEpochSdf = new SimpleDateFormat("yyyyMMddHHmmssZ")
        msEpochSdf.setTimeZone(TimeZone.getTimeZone("UTC"))

        when:
        // January 1, 1601 (UTC) which is the epoch time that Active Directory uses for attributes like lastLogonTimestamp
        def msEpoch = msEpochSdf.parse("16010101000000-0000").time

        then:
        msEpoch == AdUtil.ad1601EpochMilliseconds
    }

    void "test convert1601TimestampToInstant"() {
        given:
        // https://www.epochconverter.com/ldap says the following is: GMT: Friday, March 1, 2024 5:29:10 PM
        // On a windows machine, w32tm.exe /ntte says this is 17:29:10.6137535 - 3/1/2024
        // Rounded up to milliseconds, this would be 17:29:10.614.
        String timestamp = "133537877506137535"

        when:
        Instant instant = AdUtil.convert1601TimestampToInstant(timestamp)

        then:
        instant.toString() == "2024-03-01T17:29:10.613753500Z"
    }

    void "test convert1601TimestampToDate"() {
        given:
        String timestamp = "133537877506137535"
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ")

        when:
        Date asDate = AdUtil.convert1601TimestampToDate(timestamp)
        String asFormattedString = formatter.format(asDate)

        then:
        asFormattedString == "20240301092910.614-0800"
    }

    // reverse of the above test except the result is at ceiling millisecond precision rather than nanosecond precision
    void "test convertDateTo1601TimestampString"() {
        given:
        String asFormattedString = "20240301092910.614-0800"
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ")
        Date asDate = formatter.parse(asFormattedString)

        when:
        String adTimestampString = AdUtil.convertDateTo1601TimestampString(asDate)

        then:
        adTimestampString == "133537877506140000"
    }
}
