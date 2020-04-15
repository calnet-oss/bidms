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
package edu.berkeley.bidms.app.matchservice.config;

import edu.berkeley.bidms.app.common.config.properties.BidmsConfigProperties;
import edu.berkeley.bidms.app.common.config.properties.JmsConnectionConfigProperties;
import edu.berkeley.bidms.app.matchservice.config.properties.MatchServiceConfigProperties;
import edu.berkeley.bidms.app.matchservice.jms.ProvisionJmsTemplate;
import edu.berkeley.bidms.app.matchservice.rest.MatchEngineRestTemplate;
import edu.berkeley.bidms.app.matchservice.rest.ProvisionRestTemplate;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
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
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;

import javax.jms.ConnectionFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.Date;

@SpringBootConfiguration
public class MatchServiceConfiguration {

    private BidmsConfigProperties bidmsConfigProperties;
    private MatchServiceConfigProperties matchServiceConfigProperties;

    public MatchServiceConfiguration(BidmsConfigProperties bidmsConfigProperties, MatchServiceConfigProperties matchServiceConfigProperties) {
        this.bidmsConfigProperties = bidmsConfigProperties;
        this.matchServiceConfigProperties = matchServiceConfigProperties;
    }

    public URI getMatchEngineRestUrl() {
        if (bidmsConfigProperties.getRest() == null
                || !bidmsConfigProperties.getRest().containsKey("matchengine")
                || !bidmsConfigProperties.getRest().get("matchengine").containsKey("person")
                || bidmsConfigProperties.getRest().get("matchengine").get("person").getUrl() == null) {
            throw new RuntimeException(BidmsConfigProperties.REST_KEY + ".matchengine.person.url is not configured");
        }
        return bidmsConfigProperties.getRest().get("matchengine").get("person").getUrl();
    }

    private String getMatchEngineRestUsername() {
        if (matchServiceConfigProperties.getRest() == null ||
                !matchServiceConfigProperties.getRest().containsKey("matchengine") ||
                matchServiceConfigProperties.getRest().get("matchengine").getUsername() == null) {
            throw new RuntimeException(MatchServiceConfigProperties.REST_KEY + ".matchengine.username is not configured");
        }
        return matchServiceConfigProperties.getRest().get("matchengine").getUsername();
    }

    private String getMatchEngineRestPassword() {
        if (matchServiceConfigProperties.getRest() == null ||
                !matchServiceConfigProperties.getRest().containsKey("matchengine") ||
                matchServiceConfigProperties.getRest().get("matchengine").getPassword() == null) {
            throw new RuntimeException(MatchServiceConfigProperties.REST_KEY + ".matchengine.password is not configured");
        }
        return matchServiceConfigProperties.getRest().get("matchengine").getPassword();
    }

    @Bean
    public MatchEngineRestTemplate getMatchEngineRestTemplate(RestTemplateBuilder builder) {
        URI matchEngineRestUrl = getMatchEngineRestUrl();
        HttpHost target = new HttpHost(matchEngineRestUrl.getHost(), matchEngineRestUrl.getPort(), matchEngineRestUrl.getScheme());
        return getRestTemplateBuilder(builder, getHttpCredentialsProvider(target, getMatchEngineRestUsername(), getMatchEngineRestPassword()), getBasicAuthCache(target))
                .basicAuthentication(getMatchEngineRestUsername(), getMatchEngineRestPassword())
                .configure(new MatchEngineRestTemplate());
    }

    public URI getProvisionUidRestUrl() {
        if (bidmsConfigProperties.getRest() == null
                || !bidmsConfigProperties.getRest().containsKey("provision")
                || !bidmsConfigProperties.getRest().get("provision").containsKey("uid")
                || bidmsConfigProperties.getRest().get("provision").get("uid").getUrl() == null) {
            throw new RuntimeException(BidmsConfigProperties.REST_KEY + ".provision.uid.url is not configured");
        }
        return bidmsConfigProperties.getRest().get("provision").get("uid").getUrl();
    }

    public URI getProvisionNewUidRestUrl() {
        if (bidmsConfigProperties.getRest() == null
                || !bidmsConfigProperties.getRest().containsKey("provision")
                || !bidmsConfigProperties.getRest().get("provision").containsKey("new-uid")
                || bidmsConfigProperties.getRest().get("provision").get("new-uid").getUrl() == null) {
            throw new RuntimeException(BidmsConfigProperties.REST_KEY + ".provision.new-uid.url is not configured");
        }
        return bidmsConfigProperties.getRest().get("provision").get("new-uid").getUrl();
    }


    private String getProvisonRestUsername() {
        if (matchServiceConfigProperties.getRest() == null ||
                !matchServiceConfigProperties.getRest().containsKey("provision") ||
                matchServiceConfigProperties.getRest().get("provision").getUsername() == null) {
            throw new RuntimeException(MatchServiceConfigProperties.REST_KEY + ".provision.username is not configured");
        }
        return matchServiceConfigProperties.getRest().get("provision").getUsername();
    }

    private String getProvisionRestPassword() {
        if (matchServiceConfigProperties.getRest() == null ||
                !matchServiceConfigProperties.getRest().containsKey("provision") ||
                matchServiceConfigProperties.getRest().get("provision").getPassword() == null) {
            throw new RuntimeException(MatchServiceConfigProperties.REST_KEY + ".provision.password is not configured");
        }
        return matchServiceConfigProperties.getRest().get("provision").getPassword();
    }

    @Bean
    public ProvisionRestTemplate getProvisionRestTemplate(RestTemplateBuilder builder) {
        URI provisionRestUrl = getProvisionUidRestUrl();
        HttpHost target = new HttpHost(provisionRestUrl.getHost(), provisionRestUrl.getPort(), provisionRestUrl.getScheme());
        AuthCache authCache = getDigestAuthCache(target, "Registry Realm", getProvisionRestPassword());
        return getRestTemplateBuilder(builder, getHttpCredentialsProvider(target, getProvisonRestUsername(), getProvisionRestPassword()), authCache)
                .configure(new ProvisionRestTemplate());
    }

    @Bean
    public ConnectionFactory getJmsConnectionFactory() {
        if (bidmsConfigProperties.getJmsConnections() == null || !bidmsConfigProperties.getJmsConnections().containsKey("AMQ")) {
            throw new RuntimeException(BidmsConfigProperties.JMS_CONNECTIONS_KEY + ".AMQ is not configured");
        }
        JmsConnectionConfigProperties jmsConnectionConfig = bidmsConfigProperties.getJmsConnections().get("AMQ");
        if (jmsConnectionConfig.getBrokerUrl().startsWith("ssl")) {
            ActiveMQSslConnectionFactory amqConnectionFactory = new ActiveMQSslConnectionFactory();
            try {
                if (jmsConnectionConfig.getTrustStore() != null) {
                    amqConnectionFactory.setTrustStore(jmsConnectionConfig.getTrustStore());
                    amqConnectionFactory.setTrustStorePassword(jmsConnectionConfig.getTrustStorePassword());
                }
                if (jmsConnectionConfig.getKeyStore() != null) {
                    amqConnectionFactory.setKeyStore(jmsConnectionConfig.getKeyStore());
                    amqConnectionFactory.setKeyStorePassword(jmsConnectionConfig.getKeyStorePassword());
                }
            } catch (Exception e) {
                throw new RuntimeException("There was a problem configuring the JMS trust or key store", e);
            }
            amqConnectionFactory.setBrokerURL(jmsConnectionConfig.getBrokerUrl());
            amqConnectionFactory.setUserName(jmsConnectionConfig.getUsername());
            amqConnectionFactory.setPassword(jmsConnectionConfig.getPassword());
            return new PooledConnectionFactory(amqConnectionFactory);
        } else {
            ActiveMQConnectionFactory amqConnectionFactory = new ActiveMQConnectionFactory();
            amqConnectionFactory.setBrokerURL(jmsConnectionConfig.getBrokerUrl());
            amqConnectionFactory.setUserName(jmsConnectionConfig.getUsername());
            amqConnectionFactory.setPassword(jmsConnectionConfig.getPassword());
            return new PooledConnectionFactory(amqConnectionFactory);
        }
    }

    @Bean
    public ProvisionJmsTemplate getProvisionJmsTemplate(ConnectionFactory jmsConnectionFactory) {
        return new ProvisionJmsTemplate(jmsConnectionFactory);
    }

    private ClientHttpRequestFactory getSslClientHttpRequestFactory(CredentialsProvider credentialsProvider, AuthCache authCache) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        if (bidmsConfigProperties.getRestClient() != null && bidmsConfigProperties.getRestClient().getTrustStore() != null) {
            sslContextBuilder = sslContextBuilder.loadTrustMaterial(
                    bidmsConfigProperties.getRestClient().getTrustStore().getURL(),
                    bidmsConfigProperties.getRestClient().getTrustStorePassword().toCharArray()
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

    private CredentialsProvider getHttpCredentialsProvider(HttpHost targetHost, String username, String password) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(username, password));
        return credsProvider;
    }

    private AuthCache getBasicAuthCache(HttpHost target) {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicScheme = new BasicScheme();
        authCache.put(target, basicScheme);
        return authCache;
    }

    private AuthCache getDigestAuthCache(HttpHost target, String realm, String password) {
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

    private RestTemplateBuilder getRestTemplateBuilder(RestTemplateBuilder builder, CredentialsProvider credentialsProvider, AuthCache authCache) {
        return builder
                .requestFactory(() -> {
                    try {
                        return getSslClientHttpRequestFactory(credentialsProvider, authCache);
                    } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                        throw new RuntimeException(e);
                    }
                })
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    public void handleError(ClientHttpResponse response) throws IOException {
                        // no-op: caller of restTemplate methods checks for http response error codes
                    }
                });
    }
}
