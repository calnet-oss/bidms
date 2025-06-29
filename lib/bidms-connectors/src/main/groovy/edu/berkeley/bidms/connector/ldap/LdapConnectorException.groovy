/*
 * Copyright (c) 2017, Regents of the University of California and
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

package edu.berkeley.bidms.connector.ldap

import edu.berkeley.bidms.connector.ConnectorException
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

import javax.naming.NamingException
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Exception thrown within LdapConnector functionality.
 */
@CompileStatic
@InheritConstructors
class LdapConnectorException extends ConnectorException {
    private Pattern ldapErrorCodePattern = Pattern.compile("error code (\\d+)")
    private Pattern adErrorCodePattern = Pattern.compile("error code (\\d+) - ([0-9A-F]+)")

    private NamingException _namingException
    private Integer ldapErrorCode
    private Long adErrorCode

    /**
     * When interacting with AD, it has been observed that some
     * NamingException strings may contain characters equating to the
     * character \u0000, i.e.  the 'null' character.  These null characters
     * can be problematic for some JSON parsers.  This method removes the
     * null characters from the message string.
     */
    @Override
    String getMessage() {
        return scrubString(super.getMessage())
    }

    /**
     * When interacting with AD, it has been observed that some
     * NamingException strings may contain characters equating to the
     * character \u0000, i.e.  the 'null' character.  These null characters
     * can be undesired in a log file.  This method removes the null
     * characters from the full stack trace string.
     *
     * @return The stack trace as it would appear from {@link Exception#printStackTrace}
     *         but with null characters removed from the string.
     */
    static String getFullExceptionStackTraceAsScrubbedString(Throwable t) {
        StringWriter sWriter = new StringWriter()
        PrintWriter pWriter = new PrintWriter(sWriter)
        t.printStackTrace(pWriter)
        return scrubString(sWriter.toString())
    }

    static String scrubString(String str) {
        return str.replaceAll("\u0000", "")
    }

    /**
     * If an exception causation chain contains a
     * {@link LdapConnectorException}, this will return the first
     * occurrence of it in the chain.
     *
     * @param e The exception with a possible causation chain to search through.
     *
     * @return The first occurrence of {@link LdapConnectorException}
     *         in the chain, otherwise null.
     */
    static LdapConnectorException findLdapConnectorExceptionInChain(Exception e) {
        if (e instanceof LdapConnectorException) {
            return (LdapConnectorException) e
        }
        if (e.cause instanceof Exception) {
            return findLdapConnectorExceptionInChain((Exception) e.cause)
        } else {
            return null
        }
    }

    NamingException getNamingException() {
        if (_namingException == null) {
            Throwable _cause = cause
            while (_cause && !(_cause instanceof NamingException)) {
                _cause = _cause.cause
            }
            if (_cause instanceof NamingException) {
                this._namingException = _cause
            }
        }
        return _namingException
    }

    Integer getLdapErrorCode() {
        if (ldapErrorCode == null && getNamingException() && getNamingException().message) {
            // Unfortunately, Java doesn't provide any way to extract the
            // LDAP error code from the NamingException other than to parse
            // the exception string.
            Matcher m = ldapErrorCodePattern.matcher(cause.message)
            if (m.find()) {
                this.ldapErrorCode = m.group(1) as Integer
            }
        }

        return ldapErrorCode
    }

    String getLdapErrorMessage() {
        if (getNamingException()) {
            return getNamingException().message
        }
        return null
    }

    /**
     * This is the Active Directory error hex value that can be looked up at
     * https://msdn.microsoft.com/en-us/library/windows/desktop/ms681381(v=vs.85).aspx
     *
     * @return Error code as a Long.  To get back the hex string, use
     *         <code>Long.toHexString()</code>.
     */
    Long getActiveDirectoryErrorCode() {
        if (adErrorCode == null && getNamingException() && getNamingException().message) {
            // Unfortunately, Java doesn't provide any way to extract the
            // Active Directory error code from the NamingException other
            // than to parse the exception string.
            Matcher m = adErrorCodePattern.matcher(cause.message)
            if (m.find()) {
                this.ldapErrorCode = m.group(1) as Integer
                this.adErrorCode = Long.valueOf(m.group(2), 16)
            }
        }

        return adErrorCode
    }
}
