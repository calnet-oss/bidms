package edu.berkeley.bidms.app.restclient.service;

import edu.berkeley.bidms.app.common.config.properties.BidmsConfigProperties;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import java.util.Map;

@Service
public class MatchServiceRestClientService {

    private BidmsConfigProperties bidmsConfigProperties;

    public MatchServiceRestClientService(BidmsConfigProperties bidmsConfigProperties) {
        this.bidmsConfigProperties = bidmsConfigProperties;
    }

    /**
     * Potentially match new incoming SORObject entity with an existing uid.
     *
     * @param restTemplate  The REST client template for the endpoint.
     * @param sorKeyDataMap From <code>SorKeyData.asMap()</code>.
     * @return The response body type is a {@link Map} which is
     * representative of the endpoint JSON response.
     */
    public ResponseEntity<Map> triggerMatch(RestOperations restTemplate, Map sorKeyDataMap) {
        return restTemplate.exchange(
                RequestEntity
                        .post(bidmsConfigProperties.getRest().getMatchService().getTriggerMatch().getUrl())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(sorKeyDataMap),
                Map.class
        );
    }
}
