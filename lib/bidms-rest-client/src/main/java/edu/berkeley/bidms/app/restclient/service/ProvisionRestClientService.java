/*
 * Copyright (c) 2020, Regents of the University of California and
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
package edu.berkeley.bidms.app.restclient.service;

import edu.berkeley.bidms.app.common.config.properties.BidmsConfigProperties;
import edu.berkeley.bidms.restclient.util.SmarterURIBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProvisionRestClientService {

    private BidmsConfigProperties bidmsConfigProperties;

    public ProvisionRestClientService(BidmsConfigProperties bidmsConfigProperties) {
        this.bidmsConfigProperties = bidmsConfigProperties;
    }

    /**
     * Reprovisions an existing uid.
     *
     * @param restTemplate          The REST client template for the
     *                              endpoint.
     * @param uid                   The uid.
     * @param synchronousDownstream If provisioning to downstream systems is
     *                              synchronous.  Synchronous downstream
     *                              provisioning means the REST call won't
     *                              return a result until the downstream
     *                              provisioning attempt has completed.
     * @return The response body type is a {@link Map} which is
     * representative of the endpoint JSON response.
     */
    public ResponseEntity<Map> provisionUid(RestOperations restTemplate, String uid, boolean synchronousDownstream) {
        URI url = new SmarterURIBuilder(bidmsConfigProperties.getRest().getProvision().getUid().getUrl())
                .addParameter("uid", uid)
                .conditionalAddParameter(synchronousDownstream, "synchronousDownstream", true)
                .rbuild();
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("uid", uid);
        if (synchronousDownstream) {
            bodyMap.put("synchronousDownstream", true);
        }
        return restTemplate.exchange(
                RequestEntity
                        .post(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(bodyMap),
                Map.class
        );
    }

    /**
     * Assigns a new uid to a SORObject and provisions that uid.
     *
     * @param restTemplate          The REST client template for the
     *                              endpoint.
     * @param sorObjectId           The id of the SORObject.
     * @param synchronousDownstream If provisioning to downstream systems is
     *                              synchronous.  Synchronous downstream
     *                              provisioning means the REST call won't
     *                              return a result until the downstream
     *                              provisioning attempt has completed.
     * @return The response body type is a {@link Map} which is
     * representative of the endpoint JSON response.
     */
    public ResponseEntity<Map> provisionNewUid(RestOperations restTemplate, long sorObjectId, boolean synchronousDownstream) {
        URI url = new SmarterURIBuilder(bidmsConfigProperties.getRest().getProvision().getNewUid().getUrl())
                .addParameter("sorObjectId", sorObjectId)
                .conditionalAddParameter(synchronousDownstream, "synchronousDownstream", true)
                .rbuild();
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("sorObjectId", sorObjectId);
        if (synchronousDownstream) {
            bodyMap.put("synchronousDownstream", true);
        }
        return restTemplate.exchange(
                RequestEntity
                        .put(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(bodyMap),
                Map.class
        );
    }
}
