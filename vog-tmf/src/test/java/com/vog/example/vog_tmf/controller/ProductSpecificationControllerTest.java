package com.vog.example.vog_tmf.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.vog.example.vog_tmf.dto.ProductSpecificationTmf;
import com.vog.example.vog_tmf.exception.NotFoundException;
import com.vog.example.vog_tmf.service.ProductSpecificationService;
import com.vog.example.vog_tmf.tmf.PageWindow;

@WebMvcTest(ProductSpecificationController.class)
class ProductSpecificationControllerTest {

    private static final String BASE = "/tmf-api/productCatalogManagement/v4/productSpecification";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductSpecificationService service;

    private ProductSpecificationTmf tmf(String id, String name, String status) {
        return new ProductSpecificationTmf(id, BASE + "/" + id, "ProductSpecification", name, "d", "brand", "1.0",
                status, null, null);
    }

    @Test
    void post_returns201WithLocationAndEnvelope() throws Exception {
        when(service.create(any())).thenReturn(tmf("7", "5G Plan", "Active"));

        mockMvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"5G Plan\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BASE + "/7"))
                .andExpect(jsonPath("$.id").value("7"))
                .andExpect(jsonPath("$.href").value(BASE + "/7"))
                .andExpect(jsonPath("$.['@type']").value("ProductSpecification"));
    }

    @Test
    void get_unknownId_returns404TmfError() throws Exception {
        when(service.get(99L)).thenThrow(new NotFoundException("ProductSpecification not found: 99"));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value("ProductSpecification not found: 99"));
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
}
