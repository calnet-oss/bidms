package edu.berkeley.match.testutils

import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpResponse
import org.springframework.test.web.client.ResponseCreator

class TimeoutResponseCreator implements ResponseCreator {

    @Override
    public ClientHttpResponse createResponse(ClientHttpRequest request) throws IOException {
        throw new SocketTimeoutException('Testing timeout exception')
    }

    public static TimeoutResponseCreator withTimeout() {
        new TimeoutResponseCreator()
    }
}
