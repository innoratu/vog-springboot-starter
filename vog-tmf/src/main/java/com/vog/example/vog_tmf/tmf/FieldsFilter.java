package com.vog.example.vog_tmf.tmf;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/** TMF630 partial response: ?fields=a,b keeps those fields plus the mandatory envelope. */
public final class FieldsFilter {

    private static final Set<String> ALWAYS_KEPT = Set.of("id", "href", "@type");

    private FieldsFilter() {
    }

    public static ObjectNode apply(ObjectMapper mapper, Object dto, String fieldsParam) {
        ObjectNode full = (ObjectNode) mapper.valueToTree(dto);
        if (fieldsParam == null || fieldsParam.isBlank()) {
            return full;
        }
        Set<String> keep = new HashSet<>(ALWAYS_KEPT);
        for (String field : fieldsParam.split(",")) {
            keep.add(field.trim());
        }
        ObjectNode out = mapper.createObjectNode();
        for (Map.Entry<String, JsonNode> property : full.properties()) {
            if (keep.contains(property.getKey())) {
                out.set(property.getKey(), property.getValue());
            }
        }
        return out;
    }
}
