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
package edu.berkeley.bidms.restclient.util;

import edu.berkeley.bidms.restclient.util.hc5.Hc5RestClientUtil;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class RestClientUtil {

    private static ClientHttpRequestFactory getSslClientHttpRequestFactory(
            CredentialsProvider credentialsProvider,
            AuthCache authCache
    ) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        CloseableHttpClient httpClient = Hc5RestClientUtil.getSslHttpClient(credentialsProvider);
        return new HttpComponentsClientHttpRequestFactory(httpClient) {
            @Override
            protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
                return Hc5RestClientUtil.createHttpContext(credentialsProvider, authCache);
            }
        };
    }

    public static RestTemplateBuilder getSslRestTemplateBuilder(
            RestTemplateBuilder builder,
            CredentialsProvider credentialsProvider,
            AuthCache authCache
    ) {
        return builder
                .requestFactory(() -> {
                    try {
                        return getSslClientHttpRequestFactory(credentialsProvider, authCache);
                    } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException |
                             KeyManagementException e) {
                        throw new RuntimeException(e);
                    }
                })
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    protected void handleError(ClientHttpResponse response, HttpStatusCode statusCode, @Nullable URI url, @Nullable HttpMethod method) throws IOException {
                        // no-op: caller of restTemplate methods checks for http response error codes
                    }
                });
    }

    public static <T extends RestTemplate> T configureSslBasicAuthRestTemplate(RestTemplateBuilder builder, URI baseUrl, String username, String password, T restTemplate) {
        HttpHost target = new HttpHost(baseUrl.getScheme(), baseUrl.getHost(), baseUrl.getPort());
        return getSslRestTemplateBuilder(builder, Hc5RestClientUtil.getHttpCredentialsProvider(target, username, password), Hc5RestClientUtil.getBasicAuthCache(target))
                .basicAuthentication(username, password)
                .configure(restTemplate);
    }

    public static <T extends RestTemplate> T configureSslDigestAuthRestTemplate(RestTemplateBuilder builder, URI baseUrl, String username, String password, T restTemplate) {
        HttpHost target = new HttpHost(baseUrl.getScheme(), baseUrl.getHost(), baseUrl.getPort());
        return getSslRestTemplateBuilder(builder, Hc5RestClientUtil.getHttpCredentialsProvider(target, username, password), null)
                .configure(restTemplate);
    }
}
