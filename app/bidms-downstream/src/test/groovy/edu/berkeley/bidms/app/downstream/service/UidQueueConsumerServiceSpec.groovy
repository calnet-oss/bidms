/*
 * Copyright (c) 2025, Regents of the University of California and
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
package edu.berkeley.bidms.app.downstream.service

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.read.ListAppender
import edu.berkeley.bidms.connector.ldap.LdapConnectorException
import org.apache.activemq.command.ActiveMQMapMessage
import org.slf4j.LoggerFactory
import spock.lang.Specification

class UidQueueConsumerServiceSpec extends Specification {

    void "test when provision throws an LdapConnectorException"() {
        given:
        def ps = Mock(DownstreamProvisionService)
        def sut = new UidQueueConsumerService(ps)

        and: "a logger that captures log messages in a list appender"
        def logger = (Logger) LoggerFactory.getLogger(BaseUidQueueConsumerService)
        def listAppender = new ListAppender()
        listAppender.start()
        logger.addAppender(listAppender)

        and: "a mock JMS message"
        def msg = new ActiveMQMapMessage()
        msg.setString("downstreamSystemName", "TEST")
        msg.setString("uid", "123")

        when: "consumer is invoked"
        sut.consume(msg)

        then: "provisioner throws an LdapConnectorException with a null character in the message"
        1 * ps.provision(spock.lang.Specification._, "TEST", "123", false, true) >> { TestThrowingExceptionClass.throwTopException() }

        and: "the log contains the exception message"
        listAppender.list.size() && ((LoggingEvent) listAppender.list[0]).message.contains("this is an exception with a null")

        and: "the message has the null characters removed"
        !((LoggingEvent) listAppender.list[0]).message.bytes.any { it == 0 }
    }

    static class TestException extends Exception {
        TestException(String message) {
            super(message)
        }

        TestException(String message, Throwable cause) {
            super(message, cause)
        }
    }

    static class TestThrowingExceptionClass {
        static void throwTopException() {
            try {
                throwLdapConnectorException()
            } catch (Exception e) {
                throw new TestException(e.message, e)
            }
        }

        static void throwLdapConnectorException() {
            try {
                throwLdapConnectorExceptionCause()
            } catch (Exception e) {
                throw new LdapConnectorException(e.message, e)
            }
        }

        static void throwLdapConnectorExceptionCause() {
            throw new TestException("this is an exception with a null\n\u0000 character")
        }
    }
}
