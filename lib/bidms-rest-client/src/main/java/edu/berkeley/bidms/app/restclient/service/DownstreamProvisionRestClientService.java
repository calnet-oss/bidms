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
import edu.berkeley.bidms.restclient.BidmsRestClientResponseException;
import edu.berkeley.bidms.restclient.util.SmarterURIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class DownstreamProvisionRestClientService {
    private static final Logger log = LoggerFactory.getLogger(DownstreamProvisionRestClientService.class);

    BidmsConfigProperties bidmsConfigProperties;

    public DownstreamProvisionRestClientService(BidmsConfigProperties bidmsConfigProperties) {
        this.bidmsConfigProperties = bidmsConfigProperties;
    }

    @SuppressWarnings("rawtypes")
    public ResponseEntity<Map> provisionUid(RestOperations restTemplate, String downstreamSystemName, String uid, String eventId) {
        URI url = SmarterURIBuilder.from(
                bidmsConfigProperties.getRest().getDownstream().getProvisionUid().getUrl(),
                "/" + downstreamSystemName + "/" + uid
        ).rbuild();
        return restTemplate.exchange(
                RequestEntity
                        .put(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(eventId != null ? Map.of("eventId", eventId) : Map.of()),
                Map.class
        );
    }

    @SuppressWarnings("unchecked")
    public Map<String, ?> provisionUidCheckResponse(RestOperations restTemplate, String downstreamSystemName, String uid, String eventId) throws BidmsRestClientResponseException {
        return (Map<String, ?>) checkResponse(provisionUid(restTemplate, downstreamSystemName, uid, eventId), HttpStatus.OK);
    }

    @SuppressWarnings("rawtypes")
    public Map checkResponse(ResponseEntity<Map> response, HttpStatus expectedResponseCode) throws BidmsRestClientResponseException {
        if (response.getStatusCode() != expectedResponseCode) {
            throwRESTServiceException(response);
        }
        return response.getBody();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    void throwRESTServiceException(ResponseEntity<Map> response) throws BidmsRestClientResponseException {
        Map<?, ?> json = response.getBody();
        String errorMessage;
        if (json != null && json.containsKey("status")) {
            log.warn("Rest response json: " + json);
            errorMessage = json.get("status").toString();
        } else if (json != null && json.containsKey("errorMessage")) {
            log.warn("Rest response json: " + json);
            errorMessage = (String) json.get("errorMessage");
        } else if (json != null && json.containsKey("exceptions")) {
            log.warn("Rest response json: " + json);
            List<Map> exceptions = (List<Map>) json.get("exceptions");
            errorMessage = exceptions.size() > 0 && exceptions.get(0).containsKey("message") ? (String) exceptions.get(0).get("message") : null;
        } else if (response.getBody() != null) {
            log.warn("Rest response body: " + response.getBody());
            errorMessage = response.getBody().toString();
        } else {
            HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode().value());
            errorMessage = httpStatus.name();
            log.warn("Rest response code: " + httpStatus.name());
        }

        throw new BidmsRestClientResponseException(errorMessage, HttpStatus.valueOf(response.getStatusCode().value()), json);
    }
}
