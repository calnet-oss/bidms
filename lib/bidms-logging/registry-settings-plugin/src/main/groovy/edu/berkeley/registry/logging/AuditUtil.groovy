package edu.berkeley.registry.logging

import grails.converters.JSON
import grails.util.Metadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * AuditUtil for plain SLF4J audit log entries.
 */
abstract class AuditUtil {
    // this "Audit" logger is set up by registry-settings
    static Logger auditLog = LoggerFactory.getLogger("Audit")

    static final DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ"

    /**
     * Log an audit event operation to the audit log by converting it to JSON and logging it to the audit log via SLF4J.
     *
     * @param event An AuditEvent object that contains information about the audit log event.
     */
    static void logAuditEvent(AuditEvent event) {
        event.attrs.op = event.op.name()
        event.attrs.result = (event instanceof AuditSuccessEvent ? "success" : "fail")
        event.attrs.app = Metadata.current?.getApplicationName()
        if (event.eventId) {
            event.attrs.eventId = event.eventId
        }
        event.attrs.time = new Date().format(DATE_FORMAT)
        if (event.request) {
            event.attrs.remoteIpAddr = event.request.remoteAddr
        }
        event.attrs.loggedInUid = event.loggedInUid
        event.attrs.loggedInUsername = event.loggedInUsername ?: event.request?.remoteUser
        if (event.forUid) {
            event.attrs.forUid = event.forUid
        }
        if (event instanceof AuditFailEvent && ((AuditFailEvent) event).errorMsg) {
            event.attrs.errorMsg = event.errorMsg
        }
        auditLog.info((event.attrs as JSON).toString())
    }

    /**
     * Generates a random UUID string as an eventId.
     * @return A random UUID string.
     */
    static String createEventId() {
        return UUID.randomUUID().toString()
    }
}
