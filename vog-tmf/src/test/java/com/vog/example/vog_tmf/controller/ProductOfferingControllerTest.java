package com.vog.example.vog_tmf.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.vog.example.vog_tmf.dto.CategoryRef;
import com.vog.example.vog_tmf.dto.ProductOfferingTmf;
import com.vog.example.vog_tmf.dto.ProductSpecificationRef;
import com.vog.example.vog_tmf.exception.NotFoundException;
import com.vog.example.vog_tmf.service.ProductOfferingService;
import com.vog.example.vog_tmf.tmf.PageWindow;

@WebMvcTest(ProductOfferingController.class)
class ProductOfferingControllerTest {

    private static final String BASE = "/tmf-api/productCatalogManagement/v4/productOffering";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductOfferingService service;

    private ProductOfferingTmf tmf(String id, String name, String status) {
        return new ProductOfferingTmf(id, BASE + "/" + id, "ProductOffering", name, "d", false, true, status,
                null, null,
                new ProductSpecificationRef("3", "/tmf-api/productCatalogManagement/v4/productSpecification/3",
                        "5G SIM-Only Spec", "ProductSpecification"),
                List.of(new CategoryRef("2", "/tmf-api/productCatalogManagement/v4/category/2", "Mobile",
                        "Category")));
    }

    @Test
    void post_returns201WithLocationAndEnvelope() throws Exception {
        when(service.create(any())).thenReturn(tmf("10", "Mobile 5G Unlimited", "Active"));

        mockMvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Mobile 5G Unlimited\",\"productSpecification\":{\"id\":\"3\"},"
                        + "\"category\":[{\"id\":\"2\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BASE + "/10"))
                .andExpect(jsonPath("$.id").value("10"))
                .andExpect(jsonPath("$.href").value(BASE + "/10"))
                .andExpect(jsonPath("$.['@type']").value("ProductOffering"))
                .andExpect(jsonPath("$.productSpecification.['@referredType']").value("ProductSpecification"))
                .andExpect(jsonPath("$.category[0].href")
                        .value("/tmf-api/productCatalogManagement/v4/category/2"));
    }

    @Test
    void post_missingName_returns400TmfError() throws Exception {
        mockMvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.reason").value("Bad Request"))
                .andExpect(jsonPath("$.status").value("400"));
    }

    @Test
    void get_unknownId_returns404TmfError() throws Exception {
        when(service.get(99L)).thenThrow(new NotFoundException("ProductOffering not found: 99"));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value("ProductOffering not found: 99"));
    }

    @Test
    void list_partialPage_returns206WithCountHeaders() throws Exception {
        when(service.list(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageWindow<>(List.of(tmf("1", "A", "Active")), 3));

        mockMvc.perform(get(BASE).param("limit", "1"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string("X-Total-Count", "3"))
                .andExpect(header().string("X-Result-Count", "1"));
    }

    @Test
    void list_fieldsParam_projectsButKeepsEnvelope() throws Exception {
        when(service.list(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageWindow<>(List.of(tmf("1", "A", "Active")), 1));

        mockMvc.perform(get(BASE).param("fields", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].['@type']").value("ProductOffering"))
                .andExpect(jsonPath("$[0].lifecycleStatus").doesNotExist());
    }

    @Test
    void patch_mergePatchContentType_isAccepted() throws Exception {
        when(service.patch(eq(5L), any())).thenReturn(tmf("5", "Mobile 5G Unlimited", "Launched"));

        mockMvc.perform(patch(BASE + "/5")
                .contentType("application/merge-patch+json")
                .content("{\"lifecycleStatus\":\"Launched\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lifecycleStatus").value("Launched"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete(BASE + "/5")).andExpect(status().isNoContent());
        Mockito.verify(service).delete(5L);
    }
}
