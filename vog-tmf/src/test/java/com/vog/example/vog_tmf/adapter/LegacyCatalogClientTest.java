package com.vog.example.vog_tmf.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.vog.example.vog_tmf.exception.DownstreamUnavailableException;

class LegacyCatalogClientTest {

    private MockRestServiceServer server;
    private LegacyCatalogClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new LegacyCatalogClient(builder, "");
    }

    @Test
    void fetchCategories_mapsJsonArray() {
        server.expect(requestTo("/api/categories"))
                .andRespond(withSuccess("[{\"id\":1,\"name\":\"Mammal\",\"description\":\"d\"}]",
                        MediaType.APPLICATION_JSON));

        List<LegacyCategory> out = client.fetchCategories();

        assertThat(out).hasSize(1);
        assertThat(out.get(0).name()).isEqualTo("Mammal");
    }

    @Test
    void fetchCategories_connectionFailure_throwsDownstreamUnavailable() {
        server.expect(requestTo("/api/categories"))
                .andRespond(withException(new IOException("Connection refused")));

        assertThatThrownBy(() -> client.fetchCategories())
                .isInstanceOf(DownstreamUnavailableException.class);
    }
}
