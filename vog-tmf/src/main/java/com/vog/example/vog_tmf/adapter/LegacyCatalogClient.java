package com.vog.example.vog_tmf.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.vog.example.vog_tmf.exception.DownstreamUnavailableException;
import com.vog.example.vog_tmf.exception.NotFoundException;

@Component
public class LegacyCatalogClient {

    private final RestClient restClient;

    public LegacyCatalogClient(RestClient.Builder builder,
            @Value("${vog.legacy.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public List<LegacyCategory> fetchCategories() {
        try {
            return restClient.get().uri("/api/categories").retrieve()
                    .body(new ParameterizedTypeReference<List<LegacyCategory>>() {
                    });
        } catch (ResourceAccessException e) {
            throw unavailable(e);
        }
    }

    public LegacyCategory fetchCategory(long id) {
        try {
            return restClient.get().uri("/api/categories/{id}", id).retrieve().body(LegacyCategory.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Category not found: " + id);
        } catch (ResourceAccessException e) {
            throw unavailable(e);
        }
    }

    private DownstreamUnavailableException unavailable(Exception cause) {
        return new DownstreamUnavailableException(
                "Legacy catalog (vog-demo) unreachable: " + cause.getMessage());
    }
}
