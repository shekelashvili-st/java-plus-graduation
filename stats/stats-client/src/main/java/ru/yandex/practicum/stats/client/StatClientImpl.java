package ru.yandex.practicum.stats.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.stats.dto.EndpointHit;
import ru.yandex.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatClientImpl implements StatClient {

    private RestClient client;
    private final DiscoveryClient discoveryClient;

    @Autowired
    public StatClientImpl(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public void create(EndpointHit endpointHit) {
        ResponseEntity<Void> response = getClient().post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHit)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String uri = UriComponentsBuilder.newInstance()
                .path("/stats")
                .queryParam("start", start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .queryParam("end", end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .toUriString();

        return getClient().get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public RestClient getClient() {
        if (client == null) {
            try {
                ServiceInstance instance = discoveryClient.getInstances("stats-server").getFirst();
                String serverUrl = "https://" + instance.getHost() + ":" + instance.getPort();
                DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(serverUrl);
                uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
                client = RestClient.builder()
                        .uriBuilderFactory(uriBuilderFactory)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("stats-server unavailable");
            }
        }
        return client;
    }
}
