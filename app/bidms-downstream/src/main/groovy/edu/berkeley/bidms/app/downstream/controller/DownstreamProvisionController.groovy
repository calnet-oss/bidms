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
package edu.berkeley.bidms.app.downstream.controller

import edu.berkeley.bidms.app.downstream.service.DownstreamProvisionService
import edu.berkeley.bidms.downstream.model.request.BulkProvisionCommand
import edu.berkeley.bidms.downstream.model.request.ProvisionCommand
import edu.berkeley.bidms.downstream.service.NotFoundException
import edu.berkeley.bidms.downstream.service.ProvisioningResult
import edu.berkeley.bidms.logging.AuditFailEvent
import edu.berkeley.bidms.logging.AuditSuccessEvent
import edu.berkeley.bidms.logging.AuditUtil
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

@Slf4j
@RequestMapping(value = "/bidms-downstream")
@RestController
class DownstreamProvisionController {
    @Value('${bidms.downstream.app-name}')
    private String APP_NAME

    static enum AuditOperation {
        provisionBulk,
        provisionUid
    }

    DownstreamProvisionService provisionService

    DownstreamProvisionController(DownstreamProvisionService provisionService) {
        this.provisionService = provisionService
    }

    @PutMapping(value = '/provision/{downstreamSystemName}', consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, ?> provision(
            HttpServletRequest request,
            @PathVariable String downstreamSystemName,
            @RequestBody(required = false) BulkProvisionCommand cmd
    ) {
        return provision(request, downstreamSystemName, null, cmd)
    }

    @PutMapping(value = '/provision/{downstreamSystemName}/{uid}', consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, ?> provision(
            HttpServletRequest request,
            @PathVariable String downstreamSystemName,
            @PathVariable String uid,
            @RequestBody(required = false) ProvisionCommand cmd
    ) {
        String eventId = cmd?.eventId ?: AuditUtil.createEventId()
        AuditOperation auditOp = (uid != null ? AuditOperation.provisionUid : AuditOperation.provisionBulk)
        try {
            ProvisioningResult result = null
            if (!uid) {
                // bulk mode, which typically will provision asynchronously but tests may request synchronous bulk provisioning
                result = provisionService.provision(eventId, downstreamSystemName, uid, !((BulkProvisionCommand) cmd)?.synchronous)
            } else {
                result = provisionService.provision(eventId, downstreamSystemName, uid)
            }
            Map<String, Object> auditAttrs = new LinkedHashMap<String, Object>(result.toMap())
            AuditUtil.logAuditEvent(APP_NAME, new AuditSuccessEvent(request: request, eventId: eventId, loggedInUsername: getCurrentUsername(request),
                    op: auditOp, forUid: uid, attrs: auditAttrs))
            return result.toMap()
        }
        catch (NotFoundException e) {
            log.warn(e.message)
            AuditUtil.logAuditEvent(APP_NAME, new AuditFailEvent(request: request, eventId: eventId, loggedInUsername: getCurrentUsername(request),
                    op: auditOp, forUid: uid,
                    errorMsg: e.message, attrs: [
                    notFound: true
            ]))
            throw new edu.berkeley.bidms.app.restservice.common.response.NotFoundException()
        }
        catch (Exception e) {
            AuditUtil.logAuditEvent(APP_NAME, new AuditFailEvent(request: request, eventId: eventId, loggedInUsername: getCurrentUsername(request),
                    op: auditOp, forUid: uid,
                    errorMsg: e.message, attrs: [:]))
            throw e
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private String getCurrentUsername(HttpServletRequest request) {
        return request?.remoteUser
    }
}
