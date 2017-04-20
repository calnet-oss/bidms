package edu.berkeley.registry.logging

import javax.servlet.http.HttpServletRequest

/**
 * Audit event data to be converted into plain SLF4J log messages.
 *
 * This is an abstract class.  Use AuditSuccessEvent for successful events
 * and AuditFailEvent for failure events.
 */
abstract class AuditEvent {
    /**
     * (optional) HttpServletRequest object
     */
    HttpServletRequest request

    /**
     * (optional) Event id string.
     * Recommended to use AuditUtil.createEventId(), but not required.
     * This can be used to cross-reference chained events.
     * For example, an event may start on a front-end web app and that web-app calls a REST service on the back-end (and passes the event id in the REST call).
     * The front-end and back-end may generate audit log information for this same event.
     */
    String eventId

    /**
     * (optional) UID of logged in user.  Some backend services, like REST
     * endpoints, may use special username authentication so in these cases,
     * the uid will be null (but loggedInUsername should not be).
     */
    String loggedInUid

    /**
     * (optional) Username or calnetId of logged in user.
     */
    String loggedInUsername

    /**
     * (required) Applications define their own op enums.  This method
     * simply converts the enum to a string using op.name().  No
     * restrictions are placed on the Enum name.
     */
    Enum op

    /**
     * (optional) If the operation is on a uid, then this is the uid being
     * operated on.
     */
    String forUid

    /**
     * (required) An additional map of values that will be added to the log
     * message output JSON.  If no additional values are to be set, pass an
     * empty map.  This map must be mutable as the implementation will add
     * additional attributes to this map when the event logging method is
     * called.
     */
    Map attrs
}
