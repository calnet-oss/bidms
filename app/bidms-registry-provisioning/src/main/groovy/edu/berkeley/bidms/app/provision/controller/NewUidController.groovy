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

import edu.berkeley.bidms.app.jmsclient.service.ProvisioningJmsClientService
import edu.berkeley.bidms.app.provision.service.NewUidService
import edu.berkeley.bidms.app.restservice.common.response.BadRequestException
import edu.berkeley.bidms.logging.AuditFailEvent
import edu.berkeley.bidms.logging.AuditSuccessEvent
import edu.berkeley.bidms.logging.AuditUtil
import edu.berkeley.bidms.provision.command.NewUidCommand
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerErrorException

import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@Slf4j
@RequestMapping(value = "/registry-provisioning")
@RestController
@Validated
class NewUidController {

    static enum AuditOperation {
        newUid
    }

    @Value('${bidms.provision.app-name}')
    private String APP_NAME

    NewUidService newUidService

    ProvisioningJmsClientService provisioningJmsClientService

    NewUidController(NewUidService newUidService, ProvisioningJmsClientService provisioningJmsClientService) {
        this.newUidService = newUidService
        this.provisioningJmsClientService = provisioningJmsClientService
    }

    /**
     * Assign a new uid to a SORObject.  Typically called by the matching process.
     * 
     * <ul>
     *     <li>cmd.sorObjectId             - id of the SORObject in the database</li>
     *     <li>cmd.synchronousDownstream   - If true, reprovision the UID synchronously.</li>
     *     <li>cmd.asynchronousQueue       - If true, just place a message on the newUID queue for asynchronous processing.</li>
     *     <li>cmd.eventId                 - Optional audit eventId</li>
     * </ul>
     */
    // the match-service is sending all the parameters in the query string
    @PutMapping(value = "/newUid/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, ?> save(HttpServletRequest request, @RequestBody @Valid NewUidCommand cmd) {
        String eventId
        try {
            eventId = cmd.eventId
            if (!cmd.sorObjectId) {
                throw new BadRequestException("sorObjectId is a required parameter")
            }

            if (!eventId) {
                eventId = AuditUtil.createEventId()
            }

            // The synchronousDownstream parameter is optional.  If true,
            // then this means this service will wait for provisioning to
            // downstream to complete before returning a result.  If false
            // or null, then this service will notify the downstream
            // provisioner to reprovision, but will not wait for a result.
            if (!cmd.asynchronousQueue) {
                NewUidService.NewUidResult result = newUidService.provisionNewUid(cmd.sorObjectId, cmd.synchronousDownstream, eventId)
                Map jsonResultMap = [
                        uid                   : result.uid,
                        sorObjectId           : cmd.sorObjectId,
                        provisioningSuccessful: result.provisioningSuccessful
                ]
                if (result.provisioningSuccessful) {
                    jsonResultMap.sorPrimaryKey = result.sorPrimaryKey
                    jsonResultMap.sorName = result.sorName
                    jsonResultMap.message = "uid=$result.uid successfully created and matched to sorObjectId=${cmd.sorObjectId}, sorPrimaryKey=${result.sorPrimaryKey}, sorName=${result.sorName}.  Provisioning of SORObject data successful."
                    log.debug jsonResultMap.message
                    AuditUtil.logAuditEvent(APP_NAME, new AuditSuccessEvent(request: request, eventId: eventId, loggedInUsername: getCurrentUsername(request),
                            op: AuditOperation.newUid,
                            forUid: result.uid,
                            attrs: getAuditAttrs(cmd.sorObjectId, cmd.synchronousDownstream, cmd.asynchronousQueue) + [sorPrimaryKey: result.sorPrimaryKey, sorName: result.sorName]))
                } else {
                    jsonResultMap.sorPrimaryKey = result.sorPrimaryKey
                    jsonResultMap.sorName = result.sorName
                    jsonResultMap.message = "Provisioning of SORObject data not successful for sorObjectId=${cmd.sorObjectId}, sorPrimaryKey=${result.sorPrimaryKey}, sorName=${result.sorName}.  See provisioningErrorMessage."
                    jsonResultMap.provisioningErrorMessage = result.provisioningException.message
                    log.warn jsonResultMap.message
                    log.warn jsonResultMap.provisioningErrorMessage
                    AuditUtil.logAuditEvent(APP_NAME, new AuditFailEvent(request: request, eventId: eventId, loggedInUsername: getCurrentUsername(request),
                            op: AuditOperation.newUid,
                            errorMsg: result.provisioningException.message,
                            forUid: result.uid,
                            attrs: getAuditAttrs(cmd.sorObjectId, cmd.synchronousDownstream, cmd.asynchronousQueue)))
                }

                return jsonResultMap
            } else {
                // send to newUID queue for asynchronous processing
                provisioningJmsClientService.newUid(provisioningJmsTemplate, cmd.sorObjectId, cmd.synchronousDownstream)
                AuditUtil.logAuditEvent(APP_NAME, new AuditSuccessEvent(request: request, eventId: eventId, loggedInUsername: getCurrentUsername(request),
                        op: AuditOperation.newUid, attrs: getAuditAttrs(cmd.sorObjectId, cmd.synchronousDownstream, cmd.asynchronousQueue)))
                return [
                        message: "Successfully sent sorObjectId=${cmd.sorObjectId} to newUID queue"
                ]
            }
        }
        catch (Exception e) {
            log.error("unexpected exception", e)
            AuditUtil.logAuditEvent(APP_NAME, new AuditFailEvent(request: request, eventId: eventId, loggedInUsername: getCurrentUsername(request),
                    op: AuditOperation.newUid,
                    errorMsg: e.message, attrs: getAuditAttrs(cmd.sorObjectId, cmd.synchronousDownstream, cmd.asynchronousQueue)))
            throw new ServerErrorException(e.message)
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private String getCurrentUsername(HttpServletRequest request) {
        return request?.remoteUser
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private Map<String, Object> getAuditAttrs(Long sorObjectId, Boolean synchronousDownstream, Boolean asynchronousQueue) {
        Map<String, Object> auditAttrs = [sorObjectId: sorObjectId] as Map<String, Object>
        if (synchronousDownstream) {
            auditAttrs.synchronousDownstream = true
        }
        if (asynchronousQueue) {
            auditAttrs.asynchronousQueue = true
        }
        return auditAttrs
    }
}
