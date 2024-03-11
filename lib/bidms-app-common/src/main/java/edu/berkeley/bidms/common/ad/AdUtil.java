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
package edu.berkeley.bidms.common.ad;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Miscellaneous utility methods for handling Active Directory data.
 */
public abstract class AdUtil {

    /**
     * milliseconds since January 1, 1601 (UTC) which is the epoch time that
     * Active Directory uses for attributes like lastLogonTimestamp
     */
    private static final long AD_1601_EPOCH_MS = -11644473600000L;

    /**
     * seconds since January 1, 1601 (UTC) which is the epoch time that
     * Active Directory uses for attributes like lastLogonTimestamp
     */
    private static final long AD_1601_EPOCH_SEC = -11644473600L;

    /**
     * This converts a raw objectGUID byte array into a UUID.  The UUID in
     * string form is suitable for LDAP searches.  Using the OpenLDAP
     * {@code ldapsearch} command-line client, you can search using a
     * syntax similar to:
     * {@code ldapsearch -b "<GUID=UUID_STRING>"}
     *
     * @param g objectGUID bytes (should be 16 bytes)
     * @return A UUID object which, in string form, is suitable for LDAP searches
     */
    public static UUID convertObjectGUIDToUUID(byte[] g) {
        byte[] rearranged = new byte[]{
                g[3], g[2], g[1], g[0],
                g[5], g[4], g[7], g[6],
                g[8], g[9], g[10], g[11],
                g[12], g[13], g[14], g[15]
        };
        ByteBuffer bb = ByteBuffer.wrap(rearranged);
        return new UUID(bb.getLong(), bb.getLong());
    }

    /**
     * Get the milliseconds since January 1, 1601 (UTC) which is the epoch
     * time that Active Directory uses for attributes like
     * lastLogonTimestamp
     *
     * @return milliseconds since January 1, 1601 (UTC)
     */
    public static long getAd1601EpochMilliseconds() {
        return AD_1601_EPOCH_MS;
    }

    /**
     * Get the seconds since January 1, 1601 (UTC) which is the epoch
     * time that Active Directory uses for attributes like
     * lastLogonTimestamp
     *
     * @return milliseconds since January 1, 1601 (UTC)
     */
    public static long getAd1601EpochSeconds() {
        return AD_1601_EPOCH_SEC;
    }

    /**
     * Convert an AD "1601 timestamp" string (which is a large integer) into
     * an {@link Instant} object (UTC).  1601 timestamps are the style used for attributes
     * such as lastLogonTimestamp.
     *
     * @param adTimestamp The AD "1601 timestamp" as a string.
     * @return {@link Instant} representing the UTC timestamp with full precision.
     */
    public static Instant convert1601TimestampToInstant(String adTimestamp) {
        // lastLogonTimestamp - "The LastLogonTimestamp attribute in Active
        // Directory stores the value as a large integer representing the
        // number of 100-nanosecond intervals since January 1, 1601 (UTC)."
        // Sources:
        // https://learn.microsoft.com/en-us/answers/questions/1327515/lastlogontimestamp-attribute
        // https://learn.microsoft.com/en-us/troubleshoot/windows-server/active-directory/convert-datetime-attributes-to-standard-format
        // (Online converter tool) https://www.epochconverter.com/ldap
        BigDecimal asBig = new BigDecimal(adTimestamp);
        BigDecimal[] division = (asBig.divideAndRemainder(BigDecimal.valueOf(10000000)));
        BigDecimal javaSeconds = division[0].add(BigDecimal.valueOf(AdUtil.getAd1601EpochSeconds()));
        return Instant.ofEpochSecond(javaSeconds.longValueExact(), division[1].multiply(BigDecimal.valueOf(100)).longValueExact());
    }

    /**
     * Convert an AD "1601 timestamp" string (which is a large integer) into
     * a {@link Date} object.  1601 timestamps are the style used for attributes
     * such as lastLogonTimestamp.
     * <p>
     * Note the AD timestamps may have nanosecond precision but Java Date
     * objects only have millisecond precision, so some precision is lost
     * with this conversion.  If there are any nanoseconds, this converter
     * will use the ceiling to the next millisecond.
     *
     * @param adTimestamp The AD "1601 timestamp" as a string.
     * @return {@link Date} representing the timestamp with millisecond precision.  Some precision is lost with conversion.
     */
    public static Date convert1601TimestampToDate(String adTimestamp) {
        // we don't use Date.from() because we want to use CEILING rounding mode
        BigDecimal asBig = new BigDecimal(adTimestamp);
        BigDecimal javaMilliseconds = (asBig.divide(BigDecimal.valueOf(10000), 4, RoundingMode.UNNECESSARY)).add(BigDecimal.valueOf(getAd1601EpochMilliseconds()));
        // If there are any nanoseconds, use the ceiling to the next millisecond.  Java Date objects have millisecond precision.
        return new Date(javaMilliseconds.setScale(0, RoundingMode.CEILING).longValueExact());
    }

    /**
     * Converts a Java Date object to an AD "1601 timestamp" integer.  This
     * is the timestamp style used for attributes such as
     * lastLogonTimestamp.
     *
     * @param date Date object to convert to a AD timestamp integer.
     * @return An AD "1601 timestamp" as a BigInteger.
     */
    public static BigInteger convertDateTo1601Timestamp(Date date) {
        return ((BigInteger.valueOf(date.getTime()).subtract(BigInteger.valueOf(getAd1601EpochMilliseconds()))).multiply(BigInteger.valueOf(10000)));
    }

    /**
     * Converts a Java Date object to an AD "1601 timestamp" integer string.
     * This is the timestamp style used for attributes such as
     * lastLogonTimestamp.
     *
     * @param date Date object to convert to a AD timestamp integer string.
     * @return An AD "1601 timestamp" as an integer string.
     */
    public static String convertDateTo1601TimestampString(Date date) {
        return convertDateTo1601Timestamp(date).toString();
    }
}
