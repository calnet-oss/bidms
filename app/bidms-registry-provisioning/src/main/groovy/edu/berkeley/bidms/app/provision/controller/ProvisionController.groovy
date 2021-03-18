/*
 * Copyright (c) 2015, Regents of the University of California and
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
package edu.berkeley.bidms.app.provision.controller

import edu.berkeley.bidms.app.provision.service.AbstractProvisionService
import edu.berkeley.bidms.app.restservice.common.response.NotFoundException
import edu.berkeley.bidms.app.restservice.common.response.ServiceUnavailableException
import edu.berkeley.bidms.logging.AuditFailEvent
import edu.berkeley.bidms.logging.AuditSuccessEvent
import edu.berkeley.bidms.logging.AuditUtil
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerErrorException

import javax.servlet.http.HttpServletRequest

@Slf4j
@RequestMapping(value = "/registry-provisioning")
@RestController
class ProvisionController {

    static enum AuditOperation {
        provisionUid,
        bulkProvision
    }

    @Value('${bidms.provision.app-name}')
    private String APP_NAME

    AbstractProvisionService provisionService

    ProvisionController(AbstractProvisionService provisionService) {
        this.provisionService = provisionService
    }

    /**
     * Provision or reprovision a uid.
     *
     * @param uid (Optional) If set, reprovision a particular uid. 
     *        Otherwise bulk reprovision all uids that are marked for
     *        needing reprovisioning.
     * @param synchronousDownstream (Optional) If true, synchronously wait
     *        for downstream provisioning to complete (slow) before
     *        reporting a result.  Default is false where an asynchronous
     *        message is sent to the downstream queue for later processing.
     * @param eventId (Optional) An audit eventId.
     */
    @PostMapping(value = "/provision", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, ?> save(
        HttpServletRequest request,
        @RequestParam(required = false) String uid,
        @RequestParam(required = false) Boolean synchronousDownstream,
        @RequestParam(required = false) String eventId
    ) {
        if (!eventId) {
            eventId = AuditUtil.createEventId()
        }

        /**
         * PROFILE debug statements are detected by the
         * bin/reportProfileTimes.pl script.  If you change any PROFILE
         * messages, you'll need to change them in that script too in order
         * for the script to properly report on times.
         */
        log.debug("PROFILE: save(): ENTER")

        // If uid parameter is given, then we're provisioning a specific
        // uid.  Otherwise we're provisioning all the UIDs that need to be
        // reprovisioned.
        try {
            if (uid) {
                AuditUtil.logAuditEvent(APP_NAME, new AuditSuccessEvent(request: request, eventId: eventId, loggedInUsername: getCurrentUsername(request),
                        op: AuditOperation.provisionUid, forUid: uid, attrs: getAuditAttrs(synchronousDownstream)))
                // Provision just one uid.  The synchronousDownstream
                // parameter is optional.  If true, then this means this
                // service will wait for provisioning to downstream to
                // complete before returning a result.  If false or null,
                // then this service will notify the downstream provisioner
                // to reprovision, but will not wait for a result.
                return provisionService.provisionUid(uid, synchronousDownstream, eventId)
            } else {
                AuditUtil.logAuditEvent(APP_NAME, new AuditSuccessEvent(request: request, eventId: eventId, loggedInUsername: getCurrentUsername(request),
                        op: AuditOperation.bulkProvision, attrs: getAuditAttrs(synchronousDownstream)))
                // provision all uids from the registry toProvision service
                return provisionService.bulkProvision(synchronousDownstream, eventId)
            }
        }
        catch (AbstractProvisionService.NullResponseEndpointException e) {
            log.warn("uid not found: ${uid}")
            AuditUtil.logAuditEvent(APP_NAME, new AuditFailEvent(request: request, eventId: eventId, loggedInUsername: getCurrentUsername(request),
                    op: (uid ? AuditOperation.provisionUid : AuditOperation.bulkProvision),
                    errorMsg: e.message, forUid: uid, attrs: getAuditAttrs(synchronousDownstream)))
            throw new NotFoundException("uid not found: ${uid}")
        }
        catch (AbstractProvisionService.EndpointException e) {
            log.error("endpoint exception", e)
            AuditUtil.logAuditEvent(APP_NAME, new AuditFailEvent(request: request, eventId: eventId, loggedInUsername: getCurrentUsername(request),
                    op: (uid ? AuditOperation.provisionUid : AuditOperation.bulkProvision),
                    errorMsg: e.message, forUid: uid, attrs: getAuditAttrs(synchronousDownstream)))
            throw new ServiceUnavailableException(e.message)
        }
        catch (Exception e) {
            log.error("unexpected exception", e)
            AuditUtil.logAuditEvent(APP_NAME, new AuditFailEvent(request: request, eventId: eventId, loggedInUsername: getCurrentUsername(request),
                    op: (uid ? AuditOperation.provisionUid : AuditOperation.bulkProvision),
                    errorMsg: e.message, forUid: uid, attrs: getAuditAttrs(synchronousDownstream)))
            throw new ServerErrorException(e.message)
        }
        finally {
            log.debug("PROFILE: save(): EXIT")
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private Map<String, Object> getAuditAttrs(Boolean synchronousDownstream) {
        return (synchronousDownstream ? [synchronousDownstream: true] : [:])
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private String getCurrentUsername(HttpServletRequest request) {
        return request?.remoteUser
    }
}
