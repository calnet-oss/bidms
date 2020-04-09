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
package edu.berkeley.bidms.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.berkeley.bidms.common.json.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * AuditUtil for plain SLF4J audit log entries.
 */
public abstract class AuditUtil {
    // this "Audit" logger is set up by registry-settings
    private static Logger auditLog = LoggerFactory.getLogger("Audit");

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";

    /**
     * Log an audit event operation to the audit log by converting it to JSON
     * and logging it to the audit log via SLF4J.
     *
     * @param event          An AuditEvent object that contains information
     *                       about the audit log event.
     * @param noLoggedInUser Defaults to false, but if true, loggedInUid and
     *                       loggedInUsername won't be added to the log
     *                       message.
     */
    @SuppressWarnings("unchecked")
    public static void logAuditEvent(String appName, AuditEvent event, boolean noLoggedInUser) throws LoggingRuntimeException {
        Map attrs = event.getAttrs();
        attrs.put("op", event.getOp().name());
        attrs.put("result", event instanceof AuditSuccessEvent ? "success" : "fail");
        attrs.put("app", appName);
        if (event.getEventId() != null) {
            attrs.put("eventId", event.getEventId());
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        attrs.put("time", sdf.format(new Date()));
        if (event.getRequest() != null) {
            attrs.put("remoteIpAddr", event.getRequest().getRemoteAddr());
        }
        if (!noLoggedInUser) {
            attrs.put("loggedInUid", event.getLoggedInUid());
            attrs.put("loggedInUsername", event.getLoggedInUsername() != null ? event.getLoggedInUsername() : (event.getRequest() != null ? event.getRequest().getRemoteUser() : null));
        }
        if (event.getForUid() != null) {
            attrs.put("forUid", event.getForUid());
        }
        if (event instanceof AuditFailEvent && ((AuditFailEvent) event).getErrorMsg() != null) {
            attrs.put("errorMsg", ((AuditFailEvent) event).getErrorMsg());
        }
        try {
            auditLog.info(JsonUtil.convertMapToJson(attrs));
        } catch (JsonProcessingException e) {
            throw new LoggingRuntimeException(e);
        }
    }

    public static void logAuditEvent(String appName, AuditEvent event) throws LoggingRuntimeException {
        logAuditEvent(appName, event, false);
    }

    /**
     * Generates a random UUID string as an eventId.
     *
     * @return A random UUID string.
     */
    public static String createEventId() {
        return UUID.randomUUID().toString();
    }
}
