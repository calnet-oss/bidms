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
package edu.berkeley.bidms.app

import edu.berkeley.bidms.app.common.config.properties.BidmsConfigProperties
import edu.berkeley.bidms.app.common.config.properties.jms.JmsProperties
import edu.berkeley.bidms.app.common.config.properties.jms.endpoint.JmsDownstreamProvisioningProperties
import edu.berkeley.bidms.app.common.config.properties.jms.endpoint.JmsEndpointConfigProperties
import edu.berkeley.bidms.app.common.config.properties.jms.endpoint.JmsMatchServiceProperties
import edu.berkeley.bidms.app.common.config.properties.jms.endpoint.JmsProvisioningProperties
import edu.berkeley.bidms.app.common.config.properties.rest.RestProperties
import edu.berkeley.bidms.app.common.config.properties.rest.endpoint.RestDownstreamProvisionProperties
import edu.berkeley.bidms.app.common.config.properties.rest.endpoint.RestEndpointConfigProperties
import edu.berkeley.bidms.app.common.config.properties.rest.endpoint.RestMatchEngineProperties
import edu.berkeley.bidms.app.common.config.properties.rest.endpoint.RestMatchServiceProperties
import edu.berkeley.bidms.app.common.config.properties.rest.endpoint.RestProvisionProperties
import edu.berkeley.bidms.app.matchservice.config.MatchServiceConfiguration
import edu.berkeley.bidms.app.matchservice.config.properties.MatchServiceConfigProperties
import edu.berkeley.bidms.app.matchservice.rest.MatchEngineRestTemplate
import edu.berkeley.bidms.app.matchservice.rest.ProvisionRestTemplate
import org.jspecify.annotations.Nullable
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler

import java.time.Duration

@AutoConfigurationPackage
@SpringBootConfiguration
class MatchServiceTestConfiguration {

    @Bean
    BidmsConfigProperties getBidmsConfigProperties() {
        return new BidmsConfigProperties(
                rest: new RestProperties(
                        matchengine: new RestMatchEngineProperties(
                                baseUrl: new URI("http://localhost:8080/match-engine"),
                                person: new RestEndpointConfigProperties(url: new URI("http://localhost:8080/match-engine/person"))
                        ),
                        provision: new RestProvisionProperties(
                                baseUrl: new URI("http://localhost:8080/provisioning"),
                                uid: new RestEndpointConfigProperties(url: new URI("http://localhost:8080/provisioning/provision/save")),
                                newUid: new RestEndpointConfigProperties(url: new URI("http://localhost:8080/provisioning/newUid/save"))
                        ),
                        matchService: new RestMatchServiceProperties(
                                baseUrl: new URI("http://localhost:8080/match-service"),
                                triggerMatch: new RestEndpointConfigProperties(url: new URI("http://localhost:8080/match-service/triggerMatch"))
                        ),
                        downstream: new RestDownstreamProvisionProperties(
                                baseUrl: new URI("http://localhost:8080/downstream"),
                                provisionUid: new RestEndpointConfigProperties(url: new URI("http://localhost:8080/downstream/provisionUid"))
                        )
                ),
                jms: new JmsProperties(
                        downstream: new JmsDownstreamProvisioningProperties(
                                provisionUid: new JmsEndpointConfigProperties(
                                        queueName: "test.downstreamProvisionUid"
                                )
                        ),
                        matchService: new JmsMatchServiceProperties(
                                newSorObject: new JmsEndpointConfigProperties(
                                        queueName: "test.newSorObject"
                                )
                        ),
                        provision: new JmsProvisioningProperties(
                                provisionUid: new JmsEndpointConfigProperties(queueName: "notneeded"),
                                provisionUidBulk: new JmsEndpointConfigProperties(queueName: "notneeded")
                        )
                )
        )
    }

    @Bean
    MatchServiceConfiguration getMatchServiceConfiguration(BidmsConfigProperties bidmsConfigProperties) {
        return new MatchServiceConfiguration(bidmsConfigProperties, new MatchServiceConfigProperties())
    }

    @Bean
    MatchEngineRestTemplate getMatchEngineRestTemplate() {
        return getRestTemplateBuilder()
                .configure(new MatchEngineRestTemplate());
    }

    @Bean
    ProvisionRestTemplate getProvisionRestTemplate() {
        return getRestTemplateBuilder()
                .configure(new ProvisionRestTemplate());
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private RestTemplateBuilder getRestTemplateBuilder() {
        return new RestTemplateBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    protected void handleError(ClientHttpResponse response, HttpStatusCode statusCode, @Nullable URI url, @Nullable HttpMethod method) throws IOException {
                        // no-op: caller of restTemplate methods checks for http response error codes
                    }
                })
    }
}
