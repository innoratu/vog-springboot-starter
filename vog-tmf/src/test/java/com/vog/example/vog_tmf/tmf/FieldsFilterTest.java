package com.vog.example.vog_tmf.tmf;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

class FieldsFilterTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    record Sample(String id, String href, @JsonProperty("@type") String type, String name, String description) {
    }

    private final Sample sample = new Sample("1", "/x/1", "Sample", "Alpha", "desc");

    @Test
    void keepsRequestedFieldsPlusMandatoryEnvelope() {
        ObjectNode out = FieldsFilter.apply(mapper, sample, "name");

        assertThat(out.has("name")).isTrue();
        assertThat(out.has("description")).isFalse();
        assertThat(out.has("id")).isTrue();
        assertThat(out.has("href")).isTrue();
        assertThat(out.has("@type")).isTrue();
    }

    @Test
    void nullOrBlankFieldsReturnsFullRepresentation() {
        assertThat(FieldsFilter.apply(mapper, sample, null).has("description")).isTrue();
        assertThat(FieldsFilter.apply(mapper, sample, " ").has("description")).isTrue();
    }
}
