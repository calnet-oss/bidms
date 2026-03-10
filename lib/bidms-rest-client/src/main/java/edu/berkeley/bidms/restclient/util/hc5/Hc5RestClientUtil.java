/*
 * Copyright (c) 2026, Regents of the University of California and
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
package edu.berkeley.bidms.restclient.util.hc5;

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
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;

public class Hc5RestClientUtil {
    public static CloseableHttpClient getSslHttpClient(CredentialsProvider credentialsProvider) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        return getSslHttpClient(credentialsProvider, null, null);
    }

    public static CloseableHttpClient getSslHttpClient(
            CredentialsProvider credentialsProvider,
            URL trustStoreUrl,
            char[] trustStorePassword
    ) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        SSLContextBuilder sslContextBuilder = SSLContexts.custom();
        if (trustStoreUrl != null) {
            sslContextBuilder = sslContextBuilder.loadTrustMaterial(
                    trustStoreUrl,
                    trustStorePassword
            );
        }
        SSLContext sslContext = sslContextBuilder.build();
        DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext);
        final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsStrategy)
                .build();
        return HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectionRequestTimeout(Timeout.of(Duration.ofSeconds(20)))
                                .setResponseTimeout(Timeout.of(Duration.ofSeconds(60)))
                                .build()
                )
                .build();
    }

    public static HttpClientContext createHttpContext(
            CredentialsProvider credentialsProvider,
            AuthCache authCache
    ) {
        HttpClientContext context = HttpClientContext.create();
        // An authCache is optional and only useful when preemptive authentication is possible.
        if (authCache != null) {
            context.setAuthCache(authCache);
        }
        context.setCredentialsProvider(credentialsProvider);
        return context;
    }

    public static CredentialsProvider getHttpCredentialsProvider(HttpHost targetHost, String username, String password) {
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(username, password.toCharArray()));
        return credsProvider;
    }

    public static AuthCache getBasicAuthCache(HttpHost target) {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicScheme = new BasicScheme();
        authCache.put(target, basicScheme);
        return authCache;
    }

    /**
     * Get an Apache Http Components 5 {@link HttpClientContext} suitable
     * for SSL/TLS with basic authentication.  This may be useful if you
     * wish to use the Apache http client directly instead of using Spring's
     * REST client features.
     * <p>
     * Example Java usage:
     * <pre>
     * var ctx = RestClientUtil.getSslBasicAuthHttpContext(baseUrl, username, password);
     * try (var client = RestClientUtil.getSslHttpClient(ctx.getCredentialsProvider())) {
     *     client.execute(new HttpGet("http://urlhere.internal"), ctx, (HttpClientResponseHandler<String>) response -> {
     *         return EntityUtils.toString(response.getEntity());
     *     });
     * }
     * </pre>
     *
     * @param baseUri  Base URI for the credentials provider and the auth cache.
     * @param username username cached for requests to baseUri/*
     * @param password password cached for requests to baseUri/*
     */
    public static HttpClientContext getSslBasicAuthHttpContext(URI baseUri, String username, String password) {
        HttpHost target = new HttpHost(baseUri.getScheme(), baseUri.getHost(), baseUri.getPort());
        CredentialsProvider credentialsProvider = getHttpCredentialsProvider(target, username, password);
        AuthCache authCache = getBasicAuthCache(target);
        return createHttpContext(credentialsProvider, authCache);
    }
}
