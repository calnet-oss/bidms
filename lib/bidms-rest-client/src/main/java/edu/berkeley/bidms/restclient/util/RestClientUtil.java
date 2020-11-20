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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.DigestUtils;
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
import java.util.Date;

public class RestClientUtil {

    private static String defaultRealm = "Registry Realm";

    public static String getDefaultRealm() {
        return defaultRealm;
    }

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
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        if (trustStoreUrl != null) {
            sslContextBuilder = sslContextBuilder.loadTrustMaterial(
                    trustStoreUrl,
                    trustStorePassword
            );
        }
        SSLContext sslContext = sslContextBuilder.build();
        SSLConnectionSocketFactory socketFactory =
                new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient) {
            @Override
            protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
                HttpClientContext context = HttpClientContext.create();
                context.setAuthCache(authCache);
                context.setCredentialsProvider(credentialsProvider);
                return context;
            }
        };
    }

    private static CredentialsProvider getHttpCredentialsProvider(HttpHost targetHost, String username, String password) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(username, password));
        return credsProvider;
    }

    private static AuthCache getBasicAuthCache(HttpHost target) {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicScheme = new BasicScheme();
        authCache.put(target, basicScheme);
        return authCache;
    }

    private static AuthCache getDigestAuthCache(HttpHost target, String realm, String password) {
        AuthCache authCache = new BasicAuthCache();
        DigestScheme digestAuth = new DigestScheme();
        digestAuth.overrideParamter("realm", realm);
        digestAuth.overrideParamter("nonce", createNonce(password));
        authCache.put(target, digestAuth);
        return authCache;
    }

    private static String createNonce(String password) {
        // format of nonce is:
        // base64(expirationTime + ":" + md5Hex(expirationTime + ":" + key))
        Long expirationTime = new Date().getTime() + (60 * 1000); // 1 minute
        byte[] digest = DigestUtils.md5Digest((expirationTime + ":" + password).getBytes());
        return Base64.encodeBase64String((expirationTime + ":" + Hex.encodeHexString(digest)).getBytes());
    }

    public static RestTemplateBuilder getSslRestTemplateBuilder(RestTemplateBuilder builder, CredentialsProvider credentialsProvider, AuthCache authCache) {
        return builder
                .requestFactory(() -> {
                    try {
                        return getSslClientHttpRequestFactory(credentialsProvider, authCache);
                    } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                        throw new RuntimeException(e);
                    }
                })
                .setConnectTimeout(Duration.ofSeconds(20))
                .setReadTimeout(Duration.ofSeconds(60))
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    public void handleError(ClientHttpResponse response) throws IOException {
                        // no-op: caller of restTemplate methods checks for http response error codes
                    }
                });
    }

    public static <T extends RestTemplate> T configureSslBasicAuthRestTemplate(RestTemplateBuilder builder, URI baseUrl, String username, String password, T restTemplate) {
        HttpHost target = new HttpHost(baseUrl.getHost(), baseUrl.getPort(), baseUrl.getScheme());
        return getSslRestTemplateBuilder(builder, getHttpCredentialsProvider(target, username, password), getBasicAuthCache(target))
                .basicAuthentication(username, password)
                .configure(restTemplate);
    }

    public static <T extends RestTemplate> T configureSslDigestAuthRestTemplate(RestTemplateBuilder builder, URI baseUrl, String username, String password, T restTemplate) {
        HttpHost target = new HttpHost(baseUrl.getHost(), baseUrl.getPort(), baseUrl.getScheme());
        AuthCache authCache = getDigestAuthCache(target, getDefaultRealm(), password);
        return getSslRestTemplateBuilder(builder, getHttpCredentialsProvider(target, username, password), authCache)
                .configure(restTemplate);
    }
}
