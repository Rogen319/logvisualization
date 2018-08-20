package algrithm.sequence.controller;

import algrithm.sequence.dto.GetRequestWithTraceIDRes;
import algrithm.sequence.repository.SequenceRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@RestController
@RequestMapping("/sequences")
public class SequenceController {
    @Autowired
    private SequenceRepository sequenceRepository;

    @GetMapping()
    public GetRequestWithTraceIDRes getRequestWithTraceIDRes(@RequestParam long endTime, @RequestParam long lookback) {
        return sequenceRepository.getRequestWithTraceIDRes(endTime, lookback);
    }

    @GetMapping("/test")
    public String testK8sApi() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + "eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJhZG1pbi10b2tlbi1zZHY4OCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJhZG1pbiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjlhMzM5NzI1LWE0M2YtMTFlOC1iZGJmLTAwNTA1NmE0MDg0MCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTphZG1pbiJ9.FxoNIIrlVvn0EV5HqTwQh--QO-gL-MTEz6BXvY9hsXEUSe-HuWgwb4cokPGAxEavPLafrbEK8YiL66fT3C-xH0T43my4HN_njJEWMwmBBiFU-G5B5QwMbP7WTSXTtrpkeyQM0G8wysWFhsUsper5Ke-xX8I0eY3CBOxeMchsO7QAd-py13h_ufXVKoPZvz5wAjF_5sLMRiJfpG5FKjOTe9OgCLNCr7yCrHMvcr-TvMVcfja3Eyv3lOJIjes2jpCOwXH8xRJ-dRS0CpPfiXgYDDqgwK3SjTQ7fTOIQgZNBsmRNYmcCPWKQvuQMOlVGwf1ozlaOZmyGggvirSWKJUOlA");

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange("https://10.141.212.142:6443", HttpMethod.GET, entity, String.class);

        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(responseEntity.getBody()).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }
}
