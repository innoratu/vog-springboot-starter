package com.vog.example.vog_demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.vog.example.vog_demo.dto.OrganismResponse;
import com.vog.example.vog_demo.exception.NotFoundException;
import com.vog.example.vog_demo.service.OrganismService;

@WebMvcTest(OrganismController.class)
class OrganismControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrganismService organismService;

    @Test
    void post_validOrganism_returns201() throws Exception {
        when(organismService.create(any())).thenReturn(
                new OrganismResponse(1L, "Whale", "Balaenoptera", "Ocean", null, 1L, "Mammal"));

        mockMvc.perform(post("/api/organisms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commonName\":\"Whale\",\"categoryId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoryName").value("Mammal"));
    }

    @Test
    void post_missingCommonName_returns400() throws Exception {
        mockMvc.perform(post("/api/organisms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void get_unknownId_returns404() throws Exception {
        when(organismService.get(99L)).thenThrow(new NotFoundException("Organism not found: 99"));

        mockMvc.perform(get("/api/organisms/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
