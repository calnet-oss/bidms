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

import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;

public class RestClientUtil {

    private static ClientHttpRequestFactory getSslClientHttpRequestFactory(
            CredentialsProvider credentialsProvider,
            AuthCache authCache
    ) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return getSslClientHttpRequestFactory(credentialsProvider, authCache, null, null);
    }

    private static ClientHttpRequestFactory getSslClientHttpRequestFactory(
            CredentialsProvider credentialsProvider,
            AuthCache authCache,
            URL trustStoreUrl,
            char[] trustStorePassword
    ) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContextBuilder sslContextBuilder = SSLContexts.custom();
        if (trustStoreUrl != null) {
            sslContextBuilder = sslContextBuilder.loadTrustMaterial(
                    trustStoreUrl,
                    trustStorePassword
            );
        }
        SSLContext sslContext = sslContextBuilder.build();
        SSLConnectionSocketFactory socketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .build();
        final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(socketFactory)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectionRequestTimeout(Timeout.of(Duration.ofSeconds(20)))
                                .setResponseTimeout(Timeout.of(Duration.ofSeconds(60)))
                                .build()
                )
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient) {
            @Override
            protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
                HttpClientContext context = HttpClientContext.create();
                // An authCache is optional and only useful when preemptive authentication is possible.
                if (authCache != null) {
                    context.setAuthCache(authCache);
                }
                context.setCredentialsProvider(credentialsProvider);
                return context;
            }
        };
    }

    private static CredentialsProvider getHttpCredentialsProvider(HttpHost targetHost, String username, String password) {
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(username, password.toCharArray()));
        return credsProvider;
    }

    private static AuthCache getBasicAuthCache(HttpHost target) {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicScheme = new BasicScheme();
        authCache.put(target, basicScheme);
        return authCache;
    }

    public static RestTemplateBuilder getSslRestTemplateBuilder(RestTemplateBuilder builder, CredentialsProvider credentialsProvider, AuthCache authCache) {
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
                    public void handleError(ClientHttpResponse response) throws IOException {
                        // no-op: caller of restTemplate methods checks for http response error codes
                    }
                });
    }

    public static <T extends RestTemplate> T configureSslBasicAuthRestTemplate(RestTemplateBuilder builder, URI baseUrl, String username, String password, T restTemplate) {
        HttpHost target = new HttpHost(baseUrl.getScheme(), baseUrl.getHost(), baseUrl.getPort());
        return getSslRestTemplateBuilder(builder, getHttpCredentialsProvider(target, username, password), getBasicAuthCache(target))
                .basicAuthentication(username, password)
                .configure(restTemplate);
    }

    public static <T extends RestTemplate> T configureSslDigestAuthRestTemplate(RestTemplateBuilder builder, URI baseUrl, String username, String password, T restTemplate) {
        HttpHost target = new HttpHost(baseUrl.getScheme(), baseUrl.getHost(), baseUrl.getPort());
        return getSslRestTemplateBuilder(builder, getHttpCredentialsProvider(target, username, password), null)
                .configure(restTemplate);
    }
}
