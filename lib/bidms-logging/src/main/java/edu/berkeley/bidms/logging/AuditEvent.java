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

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Audit event data to be converted into plain SLF4J log messages.
 *
 * This is an abstract class.  Use AuditSuccessEvent for successful events
 * and AuditFailEvent for failure events.
 */
public abstract class AuditEvent {
    /**
     * (optional) HttpServletRequest object
     */
    private HttpServletRequest request;

    /**
     * (optional) Event id string.
     * Recommended to use AuditUtil.createEventId(), but not required.
     * This can be used to cross-reference chained events.
     * For example, an event may start on a front-end web app and that web-app calls a REST service on the back-end (and passes the event id in the REST call).
     * The front-end and back-end may generate audit log information for this same event.
     */
    private String eventId;

    /**
     * (optional) UID of logged in user.  Some backend services, like REST
     * endpoints, may use special username authentication so in these cases,
     * the uid will be null (but loggedInUsername should not be).
     */
    private String loggedInUid;

    /**
     * (optional) Username or calnetId of logged in user.
     */
    private String loggedInUsername;

    /**
     * (required) Applications define their own op enums.  This method
     * simply converts the enum to a string using op.name().  No
     * restrictions are placed on the Enum name.
     */
    private Enum op;

    /**
     * (optional) If the operation is on a uid, then this is the uid being
     * operated on.
     */
    private String forUid;

    /**
     * (required) An additional map of values that will be added to the log
     * message output JSON.  If no additional values are to be set, pass an
     * empty map.  This map must be mutable as the implementation will add
     * additional attributes to this map when the event logging method is
     * called.
     */
    private Map attrs;

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getLoggedInUid() {
        return loggedInUid;
    }

    public void setLoggedInUid(String loggedInUid) {
        this.loggedInUid = loggedInUid;
    }

    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }

    public Enum getOp() {
        return op;
    }

    public void setOp(Enum op) {
        this.op = op;
    }

    public String getForUid() {
        return forUid;
    }

    public void setForUid(String forUid) {
        this.forUid = forUid;
    }

    public Map getAttrs() {
        return attrs;
    }

    public void setAttrs(Map attrs) {
        this.attrs = attrs;
    }
}
