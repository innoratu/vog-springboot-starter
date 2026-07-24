package com.vog.example.vog_tmf.adapter;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.vog.example.vog_tmf.exception.DownstreamUnavailableException;

@WebMvcTest(LegacyCategoryAdapterController.class)
class LegacyCategoryAdapterControllerTest {

    private static final String BASE = "/tmf-api/productCatalogManagement/v4/legacyCategory";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LegacyCatalogClient client;

    @Test
    void list_wrapsLegacyCategoriesInTmfEnvelope() throws Exception {
        when(client.fetchCategories()).thenReturn(List.of(new LegacyCategory(1L, "Mammal", "d")));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].href").value(BASE + "/1"))
                .andExpect(jsonPath("$[0].['@type']").value("Category"))
                .andExpect(jsonPath("$[0].name").value("Mammal"))
                .andExpect(jsonPath("$[0].lifecycleStatus").value("Active"));
    }

    @Test
    void list_downstreamDown_returns503TmfError() throws Exception {
        when(client.fetchCategories())
                .thenThrow(new DownstreamUnavailableException("Legacy catalog (vog-demo) unreachable"));

        mockMvc.perform(get(BASE))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("503"))
                .andExpect(jsonPath("$.reason").value("Service Unavailable"));
    }
}
